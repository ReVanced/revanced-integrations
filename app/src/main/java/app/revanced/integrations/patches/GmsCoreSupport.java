package app.revanced.integrations.patches;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.util.Objects;

import static app.revanced.integrations.utils.StringRef.str;

public class GmsCoreSupport {
    private static final String GMS_VENDOR = "app.revanced";
    private static final String GMS_PACKAGE_NAME = GMS_VENDOR + ".android.gms";
    private static final String GMS_CORE_DOWNLOAD_LINK = "https://github.com/revanced/gmscore/releases/latest";
    private static final String DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com";
    private static final Uri GMS_PROVIDER = Uri.parse("content://" + GMS_VENDOR + ".android.gsf.gservices/prefix");

    private static void startIntent(Context context, String uriString, String message) {
        ReVancedUtils.showToastLong(message);

        var intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(uriString));
        context.startActivity(intent);
    }

    @TargetApi(26)
    public static void checkAvailability() {
        var context = Objects.requireNonNull(ReVancedUtils.getContext());

        try {
            context.getPackageManager().getPackageInfo(GMS_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException exception) {
            LogHelper.printInfo(() -> "GmsCore was not found", exception);
            startIntent(context, GMS_CORE_DOWNLOAD_LINK, str("gms_core_not_installed_warning"));

            // Gracefully exit the app, so it does not crash.
            System.exit(0);
        }


        try (var client = context.getContentResolver().acquireContentProviderClient(GMS_PROVIDER)) {
            if (client != null) return;
            LogHelper.printInfo(() -> "GmsCore is not running in the background");
            startIntent(context, DONT_KILL_MY_APP_LINK, str("gms_core_not_running_warning"));
        }
    }
}
