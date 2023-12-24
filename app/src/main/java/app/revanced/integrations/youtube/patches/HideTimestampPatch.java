package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class HideTimestampPatch {
    public static boolean hideTimestamp() {
        return SettingsEnum.HIDE_TIMESTAMP.getBoolean();
    }
}
