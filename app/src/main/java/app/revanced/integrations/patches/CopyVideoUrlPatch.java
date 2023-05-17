package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.StringRef.str;

import android.os.Build;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CopyVideoUrlPatch {

    public static void copyUrl(boolean withTimestamp) {
        try {
            StringBuilder builder = new StringBuilder("https://youtu.be/");
            builder.append(VideoInformation.getVideoId());
            if (withTimestamp) {
                final long totalSeconds = VideoInformation.getVideoTime() / 1000;
                final long hours = totalSeconds / (60 * 60);
                final long minutes = (totalSeconds / 60) % 60;
                final long seconds = totalSeconds % 60;
                builder.append("?t=");
                if (hours > 0) {
                    builder.append(hours).append("h");
                }
                if (minutes > 0) {
                    builder.append(minutes).append("m");
                }
                builder.append(seconds).append("s");
            }

            ReVancedUtils.setClipboard(builder.toString());
            // Do not show a toast if using Android 13+ as it shows it's own toast.
            // But if the user copied with a timestamp then show a toast.
            // Unfortunately this will show 2 toasts on Android 13+, but no way around this.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 || withTimestamp) {
                ReVancedUtils.showToastShort(withTimestamp
                        ? str("revanced_share_copy_url_timestamp_success")
                        : str("revanced_share_copy_url_success"));
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to generate video url", e);
        }
    }

}
