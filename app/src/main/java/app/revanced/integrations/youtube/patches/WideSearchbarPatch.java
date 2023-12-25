package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public final class WideSearchbarPatch {
    public static boolean enableWideSearchbar() {
        return Setting.WIDE_SEARCHBAR.getBoolean();
    }
}
