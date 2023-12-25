package app.revanced.integrations.youtube.patches.playback.speed;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Setting;
import app.revanced.integrations.youtube.utils.LogHelper;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public final class RememberPlaybackSpeedPatch {

    /**
     * Injection point.
     */
    public static void newVideoStarted(Object ignoredPlayerController) {
        LogHelper.printDebug(() -> "newVideoStarted");
        VideoInformation.overridePlaybackSpeed(Setting.PLAYBACK_SPEED_DEFAULT.getFloat());
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        if (Setting.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.getBoolean()) {
            Setting.PLAYBACK_SPEED_DEFAULT.saveValue(playbackSpeed);
            ReVancedUtils.showToastLong("Changed default speed to: " + playbackSpeed + "x");
        }
    }

    /**
     * Injection point.
     * Overrides the video speed.  Called after video loads, and immediately after user selects a different playback speed
     */
    public static float getPlaybackSpeedOverride() {
        return VideoInformation.getPlaybackSpeed();
    }

}
