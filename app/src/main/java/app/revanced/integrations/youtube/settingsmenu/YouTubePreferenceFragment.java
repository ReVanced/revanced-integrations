package app.revanced.integrations.youtube.settingsmenu;

import android.preference.ListPreference;
import android.preference.Preference;
import app.revanced.integrations.shared.settingsmenu.AbstractPreferenceFragment;
import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection deprecation*/
public class YouTubePreferenceFragment extends AbstractPreferenceFragment {
    @Override
    protected void initialize() {
        // If the preference was included, then initialize it based on the available playback speed
        Preference defaultSpeedPreference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
        if (defaultSpeedPreference instanceof ListPreference) {
            CustomPlaybackSpeedPatch.initializeListPreference((ListPreference) defaultSpeedPreference);
        }

        super.initialize();
    }
}
