package app.revanced.integrations.twitch.patches;


import app.revanced.integrations.twitch.settings.SettingsEnum;

public class DebugModePatch {
    public static boolean isDebugModeEnabled() {
        return SettingsEnum.DEBUG_MODE.getBoolean();
    }
}
