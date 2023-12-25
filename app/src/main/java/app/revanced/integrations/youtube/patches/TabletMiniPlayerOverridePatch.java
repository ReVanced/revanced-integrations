package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class TabletMiniPlayerOverridePatch {

    public static boolean getTabletMiniPlayerOverride(boolean original) {
        if (Settings.USE_TABLET_MINIPLAYER.getBoolean())
            return true;
        return original;
    }
}
