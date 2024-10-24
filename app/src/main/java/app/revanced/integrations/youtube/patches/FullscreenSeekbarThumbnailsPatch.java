package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class FullscreenSeekbarThumbnailsPatch {

    private static final boolean SEEKBAR_FULLSCREEN_HIGH_QUALITY_ENABLED = Settings.SEEKBAR_FULLSCREEN_HIGH_QUALITY.get();

    /**
     * Injection point.
     */
    public static boolean useHighQualityFullscreenThumbnails() {
        return SEEKBAR_FULLSCREEN_HIGH_QUALITY_ENABLED;
    }
}
