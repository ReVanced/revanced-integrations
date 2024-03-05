package app.revanced.integrations.youtube.patches;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.ImageView;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.StringRef;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.videoplayer.ExternalDownloadButton;

@SuppressWarnings("unused")
public final class DownloadsPatch {

    /**
     * Injection point.
     */
    public static boolean inAppDownloadButtonOnClick() {
        try {
            if (!Settings.EXTERNAL_DOWNLOADER_ACTION_BUTTON.get()) {
                return false;
            }

            // Utils context is the application context, and not an activity context.
            Context context = Utils.getContext();
            boolean isActivityContext = false;

            // Use the overlay button image view (which is part of an intent) if it's available.
            // Otherwise fall back on using the application context.
            ExternalDownloadButton instance = ExternalDownloadButton.getInstance();
            if (instance != null) {
                ImageView view = instance.getButtonImageView();
                if (view != null) {
                    context = view.getContext();
                    isActivityContext = true;
                }
            }
            launchExternalDownloader(context, isActivityContext);
            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "inAppDownloadButtonOnClick failure", ex);
        }
        return false;
    }

    /**
     * @param isActivityContext If the context parameter is for an Activity.  If this is false, then
     *                          the downloader is opened as a new task (which forces YT to minimize).
     */
    public static void launchExternalDownloader(Context context, boolean isActivityContext) {
        Logger.printDebug(() -> "Launching external downloader");

        // Trim string to avoid any accidental whitespace.
        var downloaderPackageName = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.get().trim();

        boolean packageEnabled = false;
        try {
            packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            Logger.printDebug(() -> "External downloader could not be found: " + error);
        }

        // If the package is not installed, show the toast
        if (!packageEnabled) {
            Utils.showToastLong(StringRef.str("revanced_external_downloader_not_installed_warning", downloaderPackageName));
            return;
        }

        // Launch intent
        try {
            String content = String.format("https://youtu.be/%s", VideoInformation.getVideoId());

            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.setPackage(downloaderPackageName);
            intent.putExtra("android.intent.extra.TEXT", content);
            if (isActivityContext) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);

            Logger.printDebug(() -> "Launched the intent with the content: " + content);
        } catch (Exception error) {
            Logger.printException(() -> "Failed to launch the intent: " + error, error);
        }
    }
}
