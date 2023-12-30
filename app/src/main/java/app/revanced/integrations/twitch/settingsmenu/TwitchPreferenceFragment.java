package app.revanced.integrations.twitch.settingsmenu;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.settingsmenu.AbstractPreferenceFragment;
import app.revanced.integrations.twitch.settings.Settings;

public class TwitchPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void initialize() {
        super.initialize();

        // Do anything that forces this apps Settings bundle to load.
        if (Settings.BLOCK_VIDEO_ADS.get()) {
            Logger.printDebug(() -> "Block video ads enabled"); // Any statement that references the app settings.
        }
    }
}
