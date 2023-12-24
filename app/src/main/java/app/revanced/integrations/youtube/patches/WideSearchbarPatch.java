package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public final class WideSearchbarPatch {
    public static boolean enableWideSearchbar() {
        return SettingsEnum.WIDE_SEARCHBAR.getBoolean();
    }
}
