package app.revanced.integrations.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.annotation.RequiresApi;

import java.net.MalformedURLException;
import java.net.URL;

import static app.revanced.integrations.shared.StringRef.str;

/**
 * @noinspection unused
 */
public class GmsCoreSupport {
    private static final String GMS_CORE_PACKAGE_NAME
            = getGmsCoreVendorGroupId() + ".android.gms";
    private static final Uri GMS_CORE_PROVIDER
            = Uri.parse("content://" + getGmsCoreVendorGroupId() + ".android.gsf.gservices/prefix");
    private static final String DONT_KILL_MY_APP_LINK
            = "https://dontkillmyapp.com";

    private static volatile boolean gmsIsNotInstalled;

    /**
     * If GmsCore is not installed.
     */
    public static boolean gmsIsNotInstalled() {
        return gmsIsNotInstalled;
    }

    private static void open(String queryOrLink) {
        Intent intent;
        try {
            // Check if queryOrLink is a valid URL.
            new URL(queryOrLink);

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(queryOrLink));
        } catch (MalformedURLException e) {
            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, queryOrLink);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.getContext().startActivity(intent);

        // Gracefully exit, otherwise without Gms the app crashes and Android can nag the user.
        System.exit(0);
    }

    /**
     * Injection point.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkGmsCoreInstalled(Activity activity) {
        try {
            // Verify, GmsCore is installed.
            PackageManager manager = activity.getPackageManager();
            manager.getPackageInfo(GMS_CORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException exception) {
            Logger.printDebug(() -> "GmsCore was not found");
            Utils.showToastLong(str("gms_core_not_installed_warning"));
            open(getGmsCoreDownload());
        } catch (Exception ex) {
            Logger.printException(() -> "checkAvailability failure", ex);
        }
    }

    private static void showDoNotKillMyAppDialog(Context context, String messageKey) {
        // Use a delay to allow the activity to finish initializing.
        // Otherwise, if device is in dark mode the dialog is missing a dark mode color scheme.
        Utils.runOnMainThreadDelayed(() -> {
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(str("gms_core_not_whitelisted_title"))
                    .setMessage(str(messageKey))
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                        open(DONT_KILL_MY_APP_LINK);
                        System.exit(0);
                    })
                    .setCancelable(false)
                    // Do not use .setCancelable(), so if something is wrong
                    // the user can use back button to dismiss the dialog (without shutting down).
                    .show();
        }, 100);
    }

    /**
     * Injection point.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkGmsCoreWhitelisted(Activity context) {
        try {
            // Check, if GmsCore is whitelisted from battery optimizations.
            var powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(GMS_CORE_PACKAGE_NAME)) {
                Logger.printDebug(() -> "GmsCore is not whitelisted from battery optimizations");
                showDoNotKillMyAppDialog(context, "gms_core_not_whitelisted_using_battery_optimizations_message");
                return;
            }

            // Check, if GmsCore is running in the background.
            try (var client = context.getContentResolver().acquireContentProviderClient(GMS_CORE_PROVIDER)) {
                if (client == null) {
                    Logger.printDebug(() -> "GmsCore is not running in the background");
                    showDoNotKillMyAppDialog(context, "gms_core_not_whitelisted_not_allowed_in_background_message");
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "checkGmsWhitelisted failure", ex);
        }
    }

    private static String getGmsCoreDownload() {
        final var vendorGroupId = getGmsCoreVendorGroupId();
        //noinspection SwitchStatementWithTooFewBranches
        switch (vendorGroupId) {
            case "app.revanced":
                return "https://github.com/revanced/gmscore/releases/latest";
            default:
                return vendorGroupId + ".android.gms";
        }
    }

    // Modified by a patch. Do not touch.
    private static String getGmsCoreVendorGroupId() {
        return "app.revanced";
    }
}
