package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {
    public static boolean enableFullScreenAmbientMode() {
        return !Setting.DISABLE_FULLSCREEN_AMBIENT_MODE.getBoolean();
    }
}
