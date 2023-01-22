package app.revanced.integrations.patches;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class MicroGSupport {
    private static final String MICROG_VENDOR = "com.mgoogle";
    private static final String MICROG_PACKAGE_NAME = "com.mgoogle.android.gms";
    private static final String VANCED_MICROG_DOWNLOAD_LINK = "https://github.com/TeamVanced/VancedMicroG/releases/latest";
    private static final String DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com";
    public static final Uri microgUri = Uri.parse("content://com.mgoogle.android.gsf.gservices/prefix");

    public static void checkAvailability() {
        var context = ReVancedUtils.getContext();
        var contentResolver = context.getContentResolver();
        var microGClient = contentResolver.acquireContentProviderClient(microgUri);
        assert context != null;
        try {
            context.getPackageManager().getPackageInfo(MICROG_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            LogHelper.printDebug(() -> "MicroG is installed on the device");

            if (microGClient == null) {
                // This means that ReVanced cannot access MicroG, probably because
                // it's not running in the background.
                
                Toast.makeText(context, str("microg_not_running_warning"), Toast.LENGTH_LONG).show();
                var intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(DONT_KILL_MY_APP_LINK));
                context.startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException exception) {
            LogHelper.printException(() -> ("MicroG was not found"), exception);
            Toast.makeText(context, str("microg_not_installed_warning"), Toast.LENGTH_LONG).show();

            var intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(VANCED_MICROG_DOWNLOAD_LINK));
            context.startActivity(intent);
        }

    }
}
