package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

public class AudioAdsPatch {
    public static boolean shouldBlockAudioAds() {
        return Settings.BLOCK_AUDIO_ADS.getBoolean();
    }
}
