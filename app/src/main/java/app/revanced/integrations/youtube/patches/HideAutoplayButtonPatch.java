package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class HideAutoplayButtonPatch {
    public static boolean isButtonShown() {
        return !Settings.HIDE_AUTOPLAY_BUTTON.getBoolean();
    }
}
