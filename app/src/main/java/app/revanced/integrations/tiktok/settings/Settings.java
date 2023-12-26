package app.revanced.integrations.tiktok.settings;

import app.revanced.integrations.shared.settings.Setting;

import static app.revanced.integrations.shared.settings.Setting.ReturnType.BOOLEAN;
import static app.revanced.integrations.shared.settings.Setting.ReturnType.STRING;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Settings {
    public static final Setting DEBUG = new Setting("debug", BOOLEAN, FALSE); // Must be first value, otherwise logging during loading will not work.
    public static final Setting REMOVE_ADS = new Setting("remove_ads", BOOLEAN, TRUE, true);
    public static final Setting HIDE_LIVE = new Setting("hide_live", BOOLEAN, FALSE, true);
    public static final Setting HIDE_STORY = new Setting("hide_story", BOOLEAN, FALSE, true);
    public static final Setting HIDE_IMAGE = new Setting("hide_image", BOOLEAN, FALSE, true);
    public static final Setting MIN_MAX_VIEWS = new Setting("min_max_views", STRING, "0-" + Long.MAX_VALUE, true);
    public static final Setting MIN_MAX_LIKES = new Setting("min_max_likes", STRING, "0-" + Long.MAX_VALUE, true);
    public static final Setting DOWNLOAD_PATH = new Setting("down_path", STRING, "DCIM/TikTok");
    public static final Setting DOWNLOAD_WATERMARK = new Setting("down_watermark", BOOLEAN, TRUE);
    public static final Setting CLEAR_DISPLAY = new Setting("clear_display", BOOLEAN, FALSE);
    public static final Setting SIM_SPOOF = new Setting("simspoof", BOOLEAN, TRUE, true);
    public static final Setting SIM_SPOOF_ISO = new Setting("simspoof_iso", STRING, "us");
    public static final Setting SIMSPOOF_MCCMNC = new Setting("simspoof_mccmnc", STRING, "310160");
    public static final Setting SIMSPOOF_OP_NAME = new Setting("simspoof_op_name", STRING, "T-Mobile");
}
