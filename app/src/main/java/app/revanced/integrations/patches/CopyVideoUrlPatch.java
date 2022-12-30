package app.revanced.integrations.patches;

import android.content.Context;
import android.widget.Toast;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.StringRef;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CopyVideoUrlPatch {
    private static String currentVideoId;
    private static long currentVideoTimestamp = 0;

    public static void setVideoId(String videoId) {
        currentVideoId = videoId;
    }

    public static void setVideoTime(long millis) {
        if (!SettingsEnum.COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN.getBoolean()) return;
        currentVideoTimestamp = millis;
    }

    public static void copyUrl(Boolean addTimestamp) {
        try {
            String url = String.format("https://youtu.be/%s", currentVideoId);
            if (addTimestamp) {
                long seconds = currentVideoTimestamp / 1000;
                url += String.format("?t=%s", seconds);
            }

            Context context = ReVancedUtils.getContext();
            if (context != null) {
                setClipboard(context, url);
                Toast.makeText(context, StringRef.str("share_copy_url_success"), Toast.LENGTH_SHORT).show();
            } else {
                LogHelper.printDebug(() -> "context was null, failed to set clipboard");
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to generate video url", e);
        }
    }

    private static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("link", text);
        clipboard.setPrimaryClip(clip);
    }
}
