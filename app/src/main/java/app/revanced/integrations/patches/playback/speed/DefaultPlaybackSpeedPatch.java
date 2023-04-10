package app.revanced.integrations.patches.playback.speed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public final class DefaultPlaybackSpeedPatch {

    @Nullable
    private static String currentVideoId;

    /**
     * Injection point.
     * Called when a new video loads.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        if (videoId.equals(currentVideoId)) {
            return;
        }
        currentVideoId = videoId;
        VideoInformation.overridePlaybackSpeed(SettingsEnum.PLAYBACK_SPEED_DEFAULT.getFloat());
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        if (SettingsEnum.PLAYBACK_SPEED_REMEMBER_LAST_SELECTED.getBoolean()) {
            SettingsEnum.PLAYBACK_SPEED_DEFAULT.saveValue(playbackSpeed);
            ReVancedUtils.showToastLong("Changing default speed to: " + playbackSpeed + "x");
        }
    }

    /**
     * Injection point.
     * Overrides the video speed.  Called after video loads, and immediately after user selects a different playback speed
     */
    public static float getPlaybackSpeedOverride() {
        return VideoInformation.getCurrentPlaybackSpeed();
    }
}
