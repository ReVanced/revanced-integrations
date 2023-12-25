package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return Settings.HIDE_SEEKBAR.getBoolean();
    }
}
