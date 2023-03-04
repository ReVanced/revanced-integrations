package app.revanced.integrations.patches.playback.speed;

import static app.revanced.integrations.utils.SharedPrefCategory.REVANCED_PREFS;

import android.widget.Toast;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;


public final class RememberPlaybackRatePatch {
    private static final String REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY = "revanced_remember_playback_rate_last_value";

    public static void rememberPlaybackRate(final float selectedPlaybackRate) {
        if (!SettingsEnum.REMEMBER_PLAYBACK_RATE_SELECTED.getBoolean()) return;

        Toast.makeText(ReVancedUtils.getContext(), "Playback rate will be remembered", Toast.LENGTH_SHORT).show();

        LogHelper.printDebug(() -> "Remembering playback rate: " + selectedPlaybackRate);
        REVANCED_PREFS.saveFloat(REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY, selectedPlaybackRate);
    }

    public static float getRememberedPlaybackRate() {
        final var playbackRateOverride = REVANCED_PREFS.getFloat(REMEMBERED_PLAYBACK_RATE_PREFERENCE_KEY, -2f);

        LogHelper.printDebug(() -> "Overriding playback rate: " + playbackRateOverride);
        return playbackRateOverride;
    }
}
