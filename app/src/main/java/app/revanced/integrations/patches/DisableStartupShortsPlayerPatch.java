package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class DisableStartupShortsPlayerPatch {
    //Used by app.revanced.patches.youtube.layout.startupshortsreset.patch.DisableStartupShortsPlayerPatch
    public static boolean disableStartupShortsPlayer() {
        return SettingsEnum.DISABLE_STARTUP_SHORTS_PLAYER.getBoolean();
    }
}
