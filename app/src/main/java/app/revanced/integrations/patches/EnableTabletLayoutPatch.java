package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class EnableTabletLayoutPatch {
    public static boolean enableTabletLayout(final boolean isTablet) {
        if (SettingsEnum.TABLET_LAYOUT.getBoolean()) return true;

        return isTablet;
    }
}
