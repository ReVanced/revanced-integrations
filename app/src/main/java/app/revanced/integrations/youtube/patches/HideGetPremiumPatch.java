package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class HideGetPremiumPatch {
    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return Setting.HIDE_GET_PREMIUM.getBoolean();
    }
}
