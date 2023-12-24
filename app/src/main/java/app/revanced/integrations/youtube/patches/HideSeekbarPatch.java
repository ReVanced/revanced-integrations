package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return SettingsEnum.HIDE_SEEKBAR.getBoolean();
    }
}
