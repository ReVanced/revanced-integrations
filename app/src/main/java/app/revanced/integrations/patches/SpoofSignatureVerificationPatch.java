package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;

public class SpoofSignatureVerificationPatch {
    /**
     * Enable/disable all workarounds that are required due to signature spoofing.
     */
    private static final boolean WORKAROUND = true;

    /**
     * Protobuf parameters used for autoplay in scrim.
     * Prepend this parameter to mute video playback (for autoplay in feed)
     */
    private static final String PROTOBUF_PARAMETER_SCRIM = "SAFgAXgB";

    /**
     * Protobuf parameter also used by
     * <a href="https://github.com/yt-dlp/yt-dlp/blob/81ca451480051d7ce1a31c017e005358345a9149/yt_dlp/extractor/youtube.py#L3602">yt-dlp</a>
     * <br>
     * Known issue: captions are positioned on upper area in the player.
     */
    private static final String PROTOBUF_PLAYER_PARAMS = "CgIQBg==";

    /**
     * Target Protobuf parameters.
     */
    private static final String[] PROTOBUF_PARAMETER_TARGETS = {
            "YAHI", // Autoplay in feed
            "SAFg"  // Autoplay in scrim
    };

    /**
     * Injection point.
     *
     * @param originalValue originalValue protobuf parameter
     */
    public static String overrideProtobufParameter(String originalValue) {
        try {
            if (!SettingsEnum.SPOOF_SIGNATURE_VERIFICATION.getBoolean()) {
                return originalValue;
            }

            LogHelper.printDebug(() -> "Original protobuf parameter value: " + originalValue);

            if (!WORKAROUND) return PROTOBUF_PLAYER_PARAMS;

            var isPlayingVideo = originalValue.contains(PROTOBUF_PLAYER_PARAMS);
            if (isPlayingVideo) return originalValue;

            boolean isPlayingFeed = containsAny(originalValue, PROTOBUF_PARAMETER_TARGETS) && PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL;
            if (isPlayingFeed) {
                // Videos in feed won't autoplay with sound.
                return PROTOBUF_PARAMETER_SCRIM + PROTOBUF_PLAYER_PARAMS;
            } else {
                // Spoof the parameter to prevent playback issues.
                return PROTOBUF_PLAYER_PARAMS;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "overrideProtobufParameter failure", ex);
        }

        return originalValue;
    }

    /**
     * Injection point.
     */
    public static boolean getSeekbarThumbnailOverrideValue() {
        return SettingsEnum.SPOOF_SIGNATURE_VERIFICATION.getBoolean();
    }

    /**
     * Injection point.
     *
     * @param view seekbar thumbnail view.  Includes both shorts and regular videos.
     */
    public static void seekbarImageViewCreated(ImageView view) {
        try {
            if (SettingsEnum.SPOOF_SIGNATURE_VERIFICATION.getBoolean()) {
                view.setVisibility(View.GONE);
                // Also hide the white border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
                ViewGroup parentLayout = (ViewGroup) view.getParent();
                parentLayout.setPadding(0, 0, 0, 0);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "seekbarImageViewCreated failure", ex);
        }
    }

}
