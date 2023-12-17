package app.revanced.tiktok.clearmode;

import app.revanced.tiktok.settings.SettingsEnum;

public class ClearModePatch {
    public static boolean getClearModeState() {
        return SettingsEnum.CLEARMODE_ENABLE.getBoolean();
    }
    public static void saveClearModeState(boolean newState) {
        SettingsEnum.CLEARMODE_ENABLE.saveValue(newState);
    }
}
