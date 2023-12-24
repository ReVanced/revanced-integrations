package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.SettingsEnum;

public class VideoAdsPatch {
    public static boolean shouldBlockVideoAds() {
        return SettingsEnum.BLOCK_VIDEO_ADS.getBoolean();
    }
}