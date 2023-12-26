package app.revanced.integrations.twitch.patches;


import app.revanced.integrations.twitch.settings.Settings;

public class DebugModePatch {
    public static boolean isDebugModeEnabled() {
        return Settings.DEBUG_MODE.getBoolean();
    }
}
