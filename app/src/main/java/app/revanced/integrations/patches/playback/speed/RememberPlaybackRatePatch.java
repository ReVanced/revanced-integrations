package app.revanced.integrations.patches.playback.speed;

import android.widget.Toast;

import androidx.annotation.NonNull;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackRatePatch {

    /**
     * The current playback rate
     */
    private static float currentPlaybackSpeed = SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED_VALUE.getFloat();

    /**
     * Injection point
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        currentPlaybackSpeed = SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED_VALUE.getFloat();
    }

    /**
     * Injection point
     */
    public static void rememberPlaybackRate(float selectedPlaybackRate) {
        try {
            LogHelper.printDebug(() -> "Playback rate changed to: " + selectedPlaybackRate);
            currentPlaybackSpeed = selectedPlaybackRate;

            if (!SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED.getBoolean()) {
                if (SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED_VALUE.getFloat()
                        != (float)SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED_VALUE.getDefaultValue()) {
                    Toast.makeText(ReVancedUtils.getContext(),
                            "Playback rate " + selectedPlaybackRate + "x applies to this video",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            SettingsEnum.REMEMBER_PLAYBACK_RATE_LAST_SELECTED_VALUE.setValue(selectedPlaybackRate);
            Toast.makeText(ReVancedUtils.getContext(),
                    "Changing default playback rate to " + selectedPlaybackRate + "x",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "rememberPlaybackRate failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static float getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }
}
