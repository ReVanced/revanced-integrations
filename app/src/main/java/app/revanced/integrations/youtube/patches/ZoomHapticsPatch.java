package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !SettingsEnum.DISABLE_ZOOM_HAPTICS.getBoolean();
    }
}
