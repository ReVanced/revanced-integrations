package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class TabletMiniPlayerOverridePatch {

    public static boolean getTabletMiniPlayerOverride(boolean original) {
        if (SettingsEnum.USE_TABLET_MINIPLAYER.getBoolean())
            return true;
        return original;
    }
}
