package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

public class VideoAdsPatch {
    public static boolean shouldBlockVideoAds() {
        return Settings.BLOCK_VIDEO_ADS.getBoolean();
    }
}