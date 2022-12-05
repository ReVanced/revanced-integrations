package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideAutoplayButtonPatch {

    public static boolean isButtonShown() {
        return SettingsEnum.AUTOPLAY_BUTTON_HIDDEN.getBoolean() == false;
    }
}
