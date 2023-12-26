package app.revanced.integrations.tiktok.cleardisplay;

import app.revanced.integrations.tiktok.settings.Settings;

public class RememberClearDisplayPatch {
    public static boolean getClearDisplayState() {
        return Settings.CLEAR_DISPLAY.getBoolean();
    }
    public static void rememberClearDisplayState(boolean newState) {
        Settings.CLEAR_DISPLAY.saveValue(newState);
    }
}
