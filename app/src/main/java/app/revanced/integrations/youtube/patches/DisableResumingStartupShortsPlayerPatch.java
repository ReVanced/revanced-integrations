package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

/** @noinspection unused*/
public class DisableResumingStartupShortsPlayerPatch {

    /**
     * Injection point.
     */
    public static boolean disableResumingStartupShortsPlayer() {
        return SettingsEnum.DISABLE_RESUMING_SHORTS_PLAYER.getBoolean();
    }
}
