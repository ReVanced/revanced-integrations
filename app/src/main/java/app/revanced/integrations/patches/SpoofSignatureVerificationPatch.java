package app.revanced.integrations.patches;

import android.widget.Toast;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.util.List;
import java.util.Map;

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
     * Injection point.
     *
     * @param originalValue originalValue protobuf parameter
     */
    public static String overrideProtobufParameter(String originalValue) {
        try {
            if (!SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return originalValue;
            }
            PlayerType player = PlayerType.getCurrent();
            LogHelper.printDebug(() -> "Original protobuf parameter value: " + originalValue + " PlayerType: " + player);
            if (originalValue.startsWith(PROTOBUF_PARAMETER_TARGET) || originalValue.length() == 0) {
                if (player == PlayerType.INLINE_MINIMAL) {
                    return PROTOBUF_PARAMETER_GENERAL; // home feed autoplay
                }
                if (player.isNoneOrHidden()) {
                    return PROTOBUF_PARAMETER_SHORTS; // short or story
                }
                return PROTOBUF_PARAMETER_SHORTS; // regular video player
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "overrideProtobufParameter failure", ex);
        }

        return originalValue;
    }


    /**
     * Injection point. Runs off the main thread.
     * <p>
     * Used to check the response code for requests made by YouTube.
     * Response code of interest is 403 that indicate a signature verification failure for the current request
     *
     * @param responseCode HTTP status code of the completed YouTube connection
     */
    public static void onResponse(int responseCode) {
        try {
            if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return; // already enabled
            }

            if (responseCode < 400 || responseCode >= 500){
                return; // everything normal
            }

            // TODO: check for the current url using UrlResponseInfo.getUrl()

            LogHelper.printDebug(() -> "YouTube HTTP status code: " + responseCode);
            SettingsEnum.SIGNATURE_SPOOFING.saveValue(true);
            ReVancedUtils.runOnMainThread(() -> {
                Toast.makeText(
                        ReVancedUtils.getContext(),
                        "Spoofing app signature to prevent playback issues", Toast.LENGTH_LONG
                ).show();

                // force video to reload, by temporarily seeking to a different location
                final long currentVideoTime = VideoInformation.getVideoTime();

                VideoInformation.seekTo(Math.min(currentVideoTime + 30000, VideoInformation.getCurrentVideoLength()));

                ReVancedUtils.runOnMainThreadDelayed(() -> VideoInformation.seekTo(currentVideoTime), 100);
            });

        } catch (Exception ex) {
            LogHelper.printException(() -> "onResponse failure", ex);
        }
    }

}
