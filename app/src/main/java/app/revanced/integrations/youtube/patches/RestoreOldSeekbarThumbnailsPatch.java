package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

@SuppressWarnings("unused")
public final class RestoreOldSeekbarThumbnailsPatch {
    public static boolean useFullscreenSeekbarThumbnails() {
        return !Setting.RESTORE_OLD_SEEKBAR_THUMBNAILS.getBoolean();
    }
}
