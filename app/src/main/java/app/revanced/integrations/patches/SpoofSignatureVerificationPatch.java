package app.revanced.integrations.patches;

import android.widget.Toast;

import java.util.List;
import java.util.Map;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SpoofSignatureVerificationPatch {

    /**
     * Protobuf parameters used by the player.
     * Known issue: YouTube client recognizes generic player as shorts video.
     */
    private static final String PROTOBUF_PARAMETER_GENERAL = "CgIQBg";

    /**
     * Protobuf parameter of shorts and YouTube stories.
     */
    private static final String PROTOBUF_PARAMETER_SHORTS = "8AEB"; // "8AEByAMTuAQP"

    /**
     * Target Protobuf parameters.
     * Used by the generic player.
     */
    private static final String PROTOBUF_PARAMETER_TARGET = "YADI";

    /**
     * Injection point
     *
     * @param original original protobuf parameter
     */
    public static String getProtoBufParameterOverride(String original) {
        try {
            if (!SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return original;
            }
            PlayerType player = PlayerType.getCurrent();
            LogHelper.printDebug(() -> "Original protobuf parameter value: " + original + " PlayerType: " + player);
            if (original.startsWith(PROTOBUF_PARAMETER_TARGET) || original.length() == 0) {
                if (player == PlayerType.INLINE_MINIMAL) {
                    return PROTOBUF_PARAMETER_GENERAL; // home feed autoplay
                }
                if (player.isNoneOrHidden()) {
                    return PROTOBUF_PARAMETER_SHORTS; // short or story
                }
                return PROTOBUF_PARAMETER_SHORTS; // regular video player
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "getProtoBufParameterOverride failure", ex);
        }

        return original;
    }


    /**
     * Injection point. Runs off the main thread.
     *
     * Used to check the status of http calls made to YouTube.
     * Calls of interest have status code in the 4xx range (usually 403),
     * which usually indicates a signature verification failure.
     *
     * @param statusCode           HTTP status code of the completed YouTube connection
     * @param urlConnectionHeaders all connection headers of the completed connection
     */
    public static void connectionCompleted(int statusCode, Map<String, List<String>> urlConnectionHeaders) {
        try {
            if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return; // already enabled
            }

            if (statusCode >= 400 && statusCode < 500) {
                LogHelper.printDebug(() -> "YouTube http status code: " + statusCode);
                SettingsEnum.SIGNATURE_SPOOFING.saveValue(true);
                ReVancedUtils.runOnMainThread(() -> {
                    Toast.makeText(ReVancedUtils.getContext(),
                            "Automatically enabling app signature spoofing", Toast.LENGTH_LONG).show();

                    // force video to reload, by temporarily seeking to a different location
                    final long currentVideoTime = VideoInformation.getVideoTime();
                    VideoInformation.seekTo(Math.max(currentVideoTime + 30000, VideoInformation.getCurrentVideoLength()));
                    ReVancedUtils.runOnMainThreadDelayed(() -> {
                        VideoInformation.seekTo(currentVideoTime);
                    }, 100);
                });
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "connectionCompleted failure", ex);
        }
    }

}
