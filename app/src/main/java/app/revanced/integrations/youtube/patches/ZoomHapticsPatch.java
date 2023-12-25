package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !Setting.DISABLE_ZOOM_HAPTICS.getBoolean();
    }
}
