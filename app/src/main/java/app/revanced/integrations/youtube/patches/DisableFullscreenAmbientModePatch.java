package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {
    public static boolean enableFullScreenAmbientMode() {
        return !SettingsEnum.DISABLE_FULLSCREEN_AMBIENT_MODE.getBoolean();
    }
}
