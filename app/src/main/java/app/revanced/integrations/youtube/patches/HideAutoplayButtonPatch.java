package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class HideAutoplayButtonPatch {
    public static boolean isButtonShown() {
        return !Setting.HIDE_AUTOPLAY_BUTTON.getBoolean();
    }
}
