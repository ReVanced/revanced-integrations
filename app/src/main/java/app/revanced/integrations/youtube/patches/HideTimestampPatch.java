package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class HideTimestampPatch {
    public static boolean hideTimestamp() {
        return Settings.HIDE_TIMESTAMP.getBoolean();
    }
}
