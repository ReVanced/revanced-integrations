package app.revanced.integrations.videoplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.StringRef;
import app.revanced.integrations.utils.LogHelper;

/* loaded from: classes6.dex */
//ToDo: Refactor
public class DownloadButton {
    static BottomControlButton _button;

    public static void initializeButton(Object obj) {
        _button = new BottomControlButton(
                obj,
                "download_button",
                SettingsEnum.DOWNLOADS_BUTTON_SHOWN.getBoolean(),
                DownloadButton::onDownloadClick
        );
    }

    public static void changeVisibility(boolean z) {
        if (_button != null) _button.changeVisibility(z);
    }

    private static void onDownloadClick(View view) {
        LogHelper.printDebug(() -> "Download button clicked");

        final var context = view.getContext();
        var downloaderPackageName = SettingsEnum.DOWNLOADS_PACKAGE_NAME.getString();

        boolean packageEnabled = false;
        try {
            assert context != null;
            packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            LogHelper.printDebug(() -> "Downloader could not be found: " + error);
        }

        // If the package is not installed, show the toast
        if (!packageEnabled) {
            Toast.makeText(context, downloaderPackageName + " " + StringRef.str("downloader_not_installed_warning"), Toast.LENGTH_LONG).show();
            return;
        }

        // Launch PowerTube intent
        try {
            String content = String.format("https://youtu.be/%s", VideoInformation.getCurrentVideoId());

            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.setPackage(downloaderPackageName);
            intent.putExtra("android.intent.extra.TEXT", content);
            context.startActivity(intent);

            LogHelper.printDebug(() -> "Launched the intent with the content: " + content);
        } catch (Exception error) {
            LogHelper.printDebug(() -> "Failed to launch the intent: " + error);
        }

        //var options = Arrays.asList("Video", "Audio").toArray(new CharSequence[0]);
        //
        //new AlertDialog.Builder(view.getContext())
        //        .setItems(options, (dialog, which) -> {
        //            LogHelper.debug(DownloadButton.class, String.valueOf(options[which]));
        //        })
        //        .show();
        // TODO: show popup and download via newpipe
    }
}

