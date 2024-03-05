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
            boolean useIntentNewTask = true;

            // Use the overlay button image view (which is part of an intent) if it's available.
            // Otherwise fall back on using the application context.
            ExternalDownloadButton instance = ExternalDownloadButton.getInstance();
            if (instance != null) {
                ImageView view = instance.getButtonImageView();
                if (view != null) {
                    context = view.getContext();
                    useIntentNewTask = false;
                }
            }
            launchExternalDownloader(context, useIntentNewTask);
            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "inAppDownloadButtonOnClick failure", ex);
        }
        return false;
    }

    /**
     * @param useIntentNewTask If the intent should use the new task flag.
     *                         Setting this to true will always minimize YT when the intent is launched,
     *                         even when using NewPipe or any other app that initially opens as an overlay.
     *                         This should be used only if the context is not that of an activity.
     */
    public static void launchExternalDownloader(Context context, boolean useIntentNewTask) {
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
            if (useIntentNewTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);

            Logger.printDebug(() -> "Launched the intent with the content: " + content);
        } catch (Exception error) {
            Logger.printException(() -> "Failed to launch the intent: " + error, error);
        }
    }
}
