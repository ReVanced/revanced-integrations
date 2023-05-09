package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;

import androidx.annotation.NonNull;

import java.util.Arrays;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CustomVideoSpeedPatch {
    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     */
    public static final float MAXIMUM_PLAYBACK_SPEED = 10;

    /**
     * Custom playback speeds.
     *
     * Values are also used by {@link RememberPlaybackSpeedPatch}.
     */
    public static float[] videoSpeeds;

    /**
     * Minimum value of {@link #videoSpeeds}
     */
    public static float minVideoSpeed;

    /**
     * Maxium value of {@link #videoSpeeds}
     */
    public static float maxVideoSpeed;

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    static {
        loadSpeeds();
    }

    private static void resetCustomSpeeds(@NonNull String toastMessage) {
        ReVancedUtils.showToastLong(toastMessage);
        SettingsEnum.PLAYBACK_SPEED_AVAILABLE.saveValue(SettingsEnum.PLAYBACK_SPEED_AVAILABLE.defaultValue);
    }

    private static void loadSpeeds() {
        try {
            String[] speedStrings = SettingsEnum.PLAYBACK_SPEED_AVAILABLE.getString()
                    .replaceAll("\\s", "").split(",");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }
            videoSpeeds = new float[speedStrings.length];
            for (int i = 0, length = speedStrings.length; i < length; i++) {
                final float speed = Float.parseFloat(speedStrings[i]);
                if (speed <= 0 || arrayContains(videoSpeeds, speed)) {
                    throw new IllegalArgumentException();
                }
                if (speed >= MAXIMUM_PLAYBACK_SPEED) {
                    resetCustomSpeeds("Custom speeds must be less than " + MAXIMUM_PLAYBACK_SPEED
                            + ".  Using default values.");
                    loadSpeeds();
                    return;
                }
                minVideoSpeed = Math.min(minVideoSpeed, speed);
                maxVideoSpeed = Math.max(maxVideoSpeed, speed);
                videoSpeeds[i] = speed;
            }
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "parse error", ex);
            resetCustomSpeeds("Invalid custom video speeds. Using default values.");
            loadSpeeds();
        }
    }

    private static boolean arrayContains(float[] array, float value) {
        for (float arrayValue : array) {
            if (arrayValue == value) return true;
        }
        return false;
    }

    /**
     * Initialize a settings preference list.
     *
     * Normally this is done during patching by creating a static xml preference list,
     * but the available playback speeds differ depending if {@link CustomVideoSpeedPatch} is applied or not.
     */
    public static void initializeListPreference(ListPreference preference) {
        if (preferenceListEntries == null) {
            float[] videoSpeeds = CustomVideoSpeedPatch.videoSpeeds;
            preferenceListEntries = new String[videoSpeeds.length];
            preferenceListEntryValues = new String[videoSpeeds.length];
            int i = 0;
            for (float speed : videoSpeeds) {
                String speedString = String.valueOf(speed);
                preferenceListEntries[i] = speedString + "x";
                preferenceListEntryValues[i] = speedString;
                i++;
            }
        }
        preference.setEntries(preferenceListEntries);
        preference.setEntryValues(preferenceListEntryValues);
    }
}
