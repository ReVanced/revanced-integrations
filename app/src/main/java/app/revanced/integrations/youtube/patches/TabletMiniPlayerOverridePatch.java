package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class TabletMiniPlayerOverridePatch {

    public static boolean getTabletMiniPlayerOverride(boolean original) {
        if (Setting.USE_TABLET_MINIPLAYER.getBoolean())
            return true;
        return original;
    }
}
