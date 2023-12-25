package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

/** @noinspection unused*/
public class DisableResumingStartupShortsPlayerPatch {

    /**
     * Injection point.
     */
    public static boolean disableResumingStartupShortsPlayer() {
        return Setting.DISABLE_RESUMING_SHORTS_PLAYER.getBoolean();
    }
}
