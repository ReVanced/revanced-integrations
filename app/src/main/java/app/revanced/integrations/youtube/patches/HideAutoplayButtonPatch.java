package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class HideAutoplayButtonPatch {
    public static boolean isButtonShown() {
        return !SettingsEnum.HIDE_AUTOPLAY_BUTTON.getBoolean();
    }
}
