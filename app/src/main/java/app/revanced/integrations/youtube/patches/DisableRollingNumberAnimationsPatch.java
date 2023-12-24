package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class DisableRollingNumberAnimationsPatch {
    /**
     * Injection point.
     */
    public static boolean disableRollingNumberAnimations() {
        return SettingsEnum.DISABLE_ROLLING_NUMBER_ANIMATIONS.getBoolean();
    }
}
