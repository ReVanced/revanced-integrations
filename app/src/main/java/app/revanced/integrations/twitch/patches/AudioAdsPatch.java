package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.SettingsEnum;

public class AudioAdsPatch {
    public static boolean shouldBlockAudioAds() {
        return SettingsEnum.BLOCK_AUDIO_ADS.getBoolean();
    }
}
