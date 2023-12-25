package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !Settings.DISABLE_ZOOM_HAPTICS.getBoolean();
    }
}
