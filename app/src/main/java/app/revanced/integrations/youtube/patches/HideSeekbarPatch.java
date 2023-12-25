package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return Setting.HIDE_SEEKBAR.getBoolean();
    }
}
