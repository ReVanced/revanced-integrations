package app.revanced.integrations.patches.playback.speed;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackSpeedPatch {

    /**
     * The current playback speed
     */
    private static float currentPlaybackSpeed = SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getFloat();

    @Nullable
    private static String currentVideoId;

    /**
     * Injection point
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        if (videoId.equals(currentVideoId)) {
            return;
        }
        currentVideoId = videoId;
        currentPlaybackSpeed = SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getFloat();
    }

    /**
     * Injection point
     */
    public static void rememberPlaybackSpeed(float selectedPlaybackSpeed) {
        try {
            LogHelper.printDebug(() -> "Playback speed changed to: " + selectedPlaybackSpeed);
            currentPlaybackSpeed = selectedPlaybackSpeed;

            if (!SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.getBoolean()) {
                if (SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getFloat()
                        != (float)SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getDefaultValue()) {
                    Toast.makeText(ReVancedUtils.getContext(),
                            "Playback speed " + selectedPlaybackSpeed + "x applies to this video",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.saveValue(selectedPlaybackSpeed);
            Toast.makeText(ReVancedUtils.getContext(),
                    "Changing default playback speed to " + selectedPlaybackSpeed + "x",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to remember playback speed", ex);
        }
    }

    /**
     * Injection point
     */
    public static float getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }
}
