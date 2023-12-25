package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public final class WideSearchbarPatch {
    public static boolean enableWideSearchbar() {
        return Settings.WIDE_SEARCHBAR.getBoolean();
    }
}
