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

            ReVancedUtils.setClipboard(url);
            if (context != null) Toast.makeText(context, StringRef.str("share_copy_url_success"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to generate video url", e);
        }
    }
}
