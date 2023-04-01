package app.revanced.integrations.patches;

import android.widget.Toast;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SpoofSignatureVerificationPatch {
    /**
     * Protobuf parameters used by the player.
     * Known issue: video preview not showing when using the seekbar.
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
            LogHelper.printDebug(() -> "Original protobuf parameter value: " + originalValue);
            if (originalValue.startsWith(PROTOBUF_PARAMETER_TARGET) || originalValue.isEmpty()) {
                return PROTOBUF_PARAMETER_SHORTS;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "overrideProtobufParameter failure", ex);
        }

        return originalValue;
    }


    /**
     * Injection point. Runs off the main thread.
     * <p>
     * Used to check the response code of video playback requests made by YouTube.
     * Response code of interest is 403 that indicate a signature verification failure for the current request
     *
     * @param responseCode HTTP status code of the completed YouTube connection
     */
    public static void onResponse(int responseCode) {
        try {
            if (responseCode < 400 || responseCode >= 500) {
                return; // everything normal
            }
            LogHelper.printDebug(() -> "YouTube HTTP status code: " + responseCode);

            if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return;  // already enabled
            }

            SettingsEnum.SIGNATURE_SPOOFING.saveValue(true);
            ReVancedUtils.runOnMainThread(() -> {
                Toast.makeText(
                        ReVancedUtils.getContext(),
                        "Spoofing app signature to prevent playback issues", Toast.LENGTH_LONG
                ).show();
                // it would be great if the video could be forcefully reloaded, but currently there is no code to do this
            });

        } catch (Exception ex) {
            LogHelper.printException(() -> "onResponse failure", ex);
        }
    }

    /**
     * Last WindowsSetting constructor values. Values are checked for change to reduce log spam.
     */
    private static int lastAp, lastAnchorHorizontalPos, lastAnchorVerticalPos;
    private static boolean lastZ1, lastZ2;

    /**
     * Injection point.
     */
    public static int[] getSubtitleWindowSettingsOverride(int ap, int anchorHorizontalPos, int anchorVerticalPos,
                                                         boolean z1, boolean z2) {
        int[] override = {ap, anchorHorizontalPos, anchorVerticalPos};

        if (SettingsEnum.DEBUG.getBoolean()) {
            if (ap != lastAp || anchorHorizontalPos != lastAnchorHorizontalPos
                    || anchorVerticalPos != lastAnchorVerticalPos
                    || z1 != lastZ1 || z2 != lastZ2) {
                LogHelper.printDebug(() -> "SubtitleWindowSettings ap:" + ap + " anchorHorizontalPos:" + anchorHorizontalPos
                        + " anchorVerticalPos:" + anchorVerticalPos + " z1:" + z1 + " z2:" + z2);
                lastAp = ap;
                lastAnchorHorizontalPos = anchorHorizontalPos;
                lastAnchorVerticalPos = anchorVerticalPos;
                lastZ1 = z1;
                lastZ2 = z2;
            }
        }

        if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean() && !PlayerType.getCurrent().isNoneOrHidden()) {
            override[0] = 34;
            override[1] = 50;
            override[2] = 95;
        }

        return override;
    }

}
