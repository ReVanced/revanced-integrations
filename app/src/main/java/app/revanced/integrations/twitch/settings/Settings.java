package app.revanced.integrations.twitch.settings;

import app.revanced.integrations.shared.settings.Setting;

import static app.revanced.integrations.shared.settings.Setting.ReturnType.BOOLEAN;
import static app.revanced.integrations.shared.settings.Setting.ReturnType.STRING;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Settings {
    /* Ads */
    public static final Setting BLOCK_VIDEO_ADS = new Setting("revanced_block_video_ads", BOOLEAN, TRUE);
    public static final Setting BLOCK_AUDIO_ADS = new Setting("revanced_block_audio_ads", BOOLEAN, TRUE);
    public static final Setting BLOCK_EMBEDDED_ADS = new Setting("revanced_block_embedded_ads", STRING, "luminous");

    /* Chat */
    public static final Setting SHOW_DELETED_MESSAGES = new Setting("revanced_show_deleted_messages", STRING, "cross-out");
    public static final Setting AUTO_CLAIM_CHANNEL_POINTS = new Setting("revanced_auto_claim_channel_points", BOOLEAN, TRUE);

    /* Misc */
    public static final Setting DEBUG_MODE = new Setting("revanced_debug_mode", BOOLEAN, FALSE, true);

    public static final String REVANCED_PREFS = "revanced_prefs";
}
