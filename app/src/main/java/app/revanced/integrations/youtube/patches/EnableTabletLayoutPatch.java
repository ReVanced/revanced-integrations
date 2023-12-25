package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public final class EnableTabletLayoutPatch {
    public static boolean enableTabletLayout() {
        return Setting.TABLET_LAYOUT.getBoolean();
    }
}
