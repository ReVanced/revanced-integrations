package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.SettingsEnum;

public class AutoClaimChannelPointsPatch {
    public static boolean shouldAutoClaim() {
        return SettingsEnum.AUTO_CLAIM_CHANNEL_POINTS.getBoolean();
    }
}
