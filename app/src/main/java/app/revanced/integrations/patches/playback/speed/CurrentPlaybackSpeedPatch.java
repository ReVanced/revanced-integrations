package app.revanced.integrations.patches.playback.speed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class CurrentPlaybackSpeedPatch {

    private static float DEFAULT_YOUTUBE_PLAYBACK_SPEED = 1.0f;

    /**
     * The current playback speed
     */
    private static float currentPlaybackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;

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
        currentPlaybackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        LogHelper.printDebug(() -> "User selected playback speed: " + playbackSpeed);
        currentPlaybackSpeed = playbackSpeed;
    }

    /**
     * Sets the current playback speed.
     *
     * Only to be used by {@link RememberPlaybackSpeedPatch}
     */
    static void setCurrentPlaybackSpeed(float playbackSpeed) {
        if (currentPlaybackSpeed != playbackSpeed) {
            LogHelper.printDebug(() -> "Overriding playback speed to: " + playbackSpeed);
            currentPlaybackSpeed = playbackSpeed;
        }
    }

    /**
     * @return The currently set playback speed.
     */
    public static float getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }
}
