package app.revanced.tiktok.clearmode;

import app.revanced.tiktok.settings.SettingsEnum;

public class RememberClearModePatch {
    public static boolean getClearModeState() {
        return SettingsEnum.CLEARMODE.getBoolean();
    }
    public static void rememberClearModeState(boolean newState) {
        SettingsEnum.CLEARMODE.saveValue(newState);
    }
}
