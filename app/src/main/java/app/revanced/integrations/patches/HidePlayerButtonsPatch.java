package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class HidePlayerButtonsPatch {

    /**
     * Injection point.
     */
    public static boolean previousOrNextButtonIsVisible(boolean previousOrNextButtonHidden) {
        if (SettingsEnum.HIDE_PLAYER_BUTTONS.getBoolean()) {
            return false;
        }
        return previousOrNextButtonHidden;
    }
}
