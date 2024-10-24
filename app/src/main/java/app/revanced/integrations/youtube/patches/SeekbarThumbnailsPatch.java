package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SeekbarThumbnailsPatch {

    private static final boolean SEEKBAR_THUMBNAILS_HIGH_QUALITY_ENABLED = Settings.SEEKBAR_THUMBNAILS_HIGH_QUALITY.get();

    /**
     * Injection point.
     */
    public static boolean useHighQualityFullscreenThumbnails() {
        return SEEKBAR_THUMBNAILS_HIGH_QUALITY_ENABLED;
    }
}
