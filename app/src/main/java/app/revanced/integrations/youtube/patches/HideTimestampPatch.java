package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class HideTimestampPatch {
    public static boolean hideTimestamp() {
        return Setting.HIDE_TIMESTAMP.getBoolean();
    }
}
