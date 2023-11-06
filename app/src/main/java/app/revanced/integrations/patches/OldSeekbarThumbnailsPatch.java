package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

@SuppressWarnings("unused")
public final class OldSeekbarThumbnailsPatch {
    public static boolean enableOldSeekbarThumbnails() {
        return !SettingsEnum.OLD_SEEKBAR_THUMBNAILS.getBoolean();
    }
}
