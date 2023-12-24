package app.revanced.integrations.tiktok.cleardisplay;

import app.revanced.integrations.tiktok.settings.SettingsEnum;

public class RememberClearDisplayPatch {
    public static boolean getClearDisplayState() {
        return SettingsEnum.CLEAR_DISPLAY.getBoolean();
    }
    public static void rememberClearDisplayState(boolean newState) {
        SettingsEnum.CLEAR_DISPLAY.saveValue(newState);
    }
}
