package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class HideGetPremiumPatch {
    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return SettingsEnum.HIDE_GET_PREMIUM.getBoolean();
    }
}
