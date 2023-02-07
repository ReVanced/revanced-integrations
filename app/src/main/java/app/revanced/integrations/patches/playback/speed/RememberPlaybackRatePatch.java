package app.revanced.integrations.patches.playback.speed;

import static app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames.REVANCED_PREFS;
import static app.revanced.integrations.utils.SharedPrefHelper.getFloat;
import static app.revanced.integrations.utils.SharedPrefHelper.saveFloat;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackRatePatch {
    private static final String REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY = "revanced_remember_playback_rate_last_value";

    private static float currentPlaybackRate;
    static {
        float rate = getRememberedPlaybackRate();
        if (rate > 0) {
            currentPlaybackRate = rate;
        } else {
            currentPlaybackRate = 1; // no rate has been remembered
        }
    }

    /**
     * @return the current video playback rate
     */
    public static float getCurrentPlaybackRate() {
        return currentPlaybackRate;
    }

    /**
     * Injection point.
     */
    public static void rememberPlaybackRate(final float selectedPlaybackRate) {
        currentPlaybackRate = selectedPlaybackRate;
        if (!SettingsEnum.REMEMBER_PLAYBACK_RATE_SELECTED.getBoolean()) return;

        ReVancedUtils.showToastShort("Playback rate will be remembered");

        LogHelper.printDebug(() -> "Remembering playback rate: " + selectedPlaybackRate);
        saveFloat(REVANCED_PREFS, REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY, selectedPlaybackRate);
    }

    /**
     * Injection point.
     */
    public static float getRememberedPlaybackRate() {
        // a negative value indicates no rate has been remembered
        final var playbackRateOverride = getFloat(REVANCED_PREFS, REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY, -2f);
        if (playbackRateOverride > 0) {
            LogHelper.printDebug(() -> "Overriding playback rate: " + playbackRateOverride);
        }
        return playbackRateOverride;
    }
}
