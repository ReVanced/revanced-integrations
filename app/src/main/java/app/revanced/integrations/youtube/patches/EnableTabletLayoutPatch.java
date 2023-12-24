package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public final class EnableTabletLayoutPatch {
    public static boolean enableTabletLayout() {
        return SettingsEnum.TABLET_LAYOUT.getBoolean();
    }
}
