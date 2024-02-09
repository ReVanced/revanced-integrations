package app.revanced.integrations.music.settings.preference;

import app.revanced.integrations.music.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.settings.preference.AbstractPreferenceFragment;

/**
 * Preference fragment for ReVanced settings.
 */
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {
    @Override
    protected void initialize() {
        super.initialize();

        // Do anything that forces this apps Settings bundle to load.
        if (Settings.DEBUG.get()) {
            Logger.printDebug(() -> "Debug logging enabled"); // Any statement that references the app settings.
        }
    }
}
