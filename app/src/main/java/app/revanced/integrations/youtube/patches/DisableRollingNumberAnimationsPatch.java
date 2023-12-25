package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class DisableRollingNumberAnimationsPatch {
    /**
     * Injection point.
     */
    public static boolean disableRollingNumberAnimations() {
        return Setting.DISABLE_ROLLING_NUMBER_ANIMATIONS.getBoolean();
    }
}
