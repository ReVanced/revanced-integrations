package app.revanced.integrations.settings;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", Environment.getExternalStorageDirectory().getPath() + "/Download", ReturnType.STRING),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads", true, ReturnType.BOOLEAN, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", "org.schabi.newpipe" /* NewPipe */, ReturnType.STRING),

    //Video Settings
    OLD_STYLE_QUALITY_SETTINGS("revanced_use_old_style_quality_settings", true, ReturnType.BOOLEAN),
    PREFERRED_VIDEO_SPEED("revanced_pref_video_speed", -2.0f, ReturnType.FLOAT),

    //Whitelist Settings
    //ToDo: Not used atm, Patch missing
    ENABLE_WHITELIST("revanced_whitelist_ads_enabled", false, ReturnType.BOOLEAN),

    //Ad settings
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", true, ReturnType.BOOLEAN, true),
    GENERAL_ADS_REMOVAL("revanced_general_ads_removal", true, ReturnType.BOOLEAN, true),
    MERCHANDISE_REMOVAL("revanced_merchandise", true, ReturnType.BOOLEAN, true),
    COMMUNITY_POSTS_REMOVAL("revanced_community_posts_removal", true, ReturnType.BOOLEAN, true),
    COMPACT_BANNER_REMOVAL("revanced_compact_banner_removal", true, ReturnType.BOOLEAN, true),
    COMMENTS_REMOVAL("revanced_comments_removal", false, ReturnType.BOOLEAN, true),
    MOVIE_REMOVAL("revanced_movie", true, ReturnType.BOOLEAN, true),
    FEED_SURVEY_REMOVAL("revanced_feed_survey", true, ReturnType.BOOLEAN, true),
    SHORTS_SHELF("revanced_shorts_shelf", true, ReturnType.BOOLEAN, true),
    COMMUNITY_GUIDELINES("revanced_community_guidelines", true, ReturnType.BOOLEAN, true),
    EMERGENCY_BOX_REMOVAL("revanced_emergency_box_removal", true, ReturnType.BOOLEAN, true),
    INFO_PANEL_REMOVAL("revanced_info_panel", true, ReturnType.BOOLEAN, true),
    MEDICAL_PANEL_REMOVAL("revanced_medical_panel", true, ReturnType.BOOLEAN, true),
    PAID_CONTECT_REMOVAL("revanced_paid_content", true, ReturnType.BOOLEAN, true),
    SUGGESTED_FOR_YOU_REMOVAL("revanced_suggested", true, ReturnType.BOOLEAN, true),
    HIDE_SUGGESTIONS("revanced_hide_suggestions", true, ReturnType.BOOLEAN, true),
    HIDE_LATEST_POSTS("revanced_hide_latest_posts", true, ReturnType.BOOLEAN, true),
    HIDE_CHANNEL_GUIDELINES("revanced_hide_channel_guidelines", true, ReturnType.BOOLEAN, true),

    //Layout settings
    ENDSCREEN_SHOWN("revanced_endscreen_enabled", false, ReturnType.BOOLEAN, true),
    REEL_BUTTON_SHOWN("revanced_reel_button_enabled", false, ReturnType.BOOLEAN, true),
    INFO_CARDS_SHOWN("revanced_info_cards_enabled", false, ReturnType.BOOLEAN),
    BRANDING_SHOWN("revanced_branding_watermark_enabled", false, ReturnType.BOOLEAN),
    CAST_BUTTON_SHOWN("revanced_cast_button_enabled", false, ReturnType.BOOLEAN, true),
    AUTOPLAY_BUTTON_SHOWN("revanced_autoplay_button_enabled", false, ReturnType.BOOLEAN, true),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", false, ReturnType.BOOLEAN, true),
    CREATE_BUTTON_ENABLED("revanced_create_button_enabled", false, ReturnType.BOOLEAN, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", false, ReturnType.BOOLEAN, true),
    SHORTS_BUTTON_SHOWN("revanced_shorts_button_enabled", false, ReturnType.BOOLEAN, true),
    FULLSCREEN_PANELS_SHOWN("revanced_fullscreen_panels_enabled", false, ReturnType.BOOLEAN), //ToDo: Add to prefs
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", false, ReturnType.BOOLEAN),
    HIDE_TIME_AND_SEEKBAR("revanced_hide_time_and_seekbar", false, ReturnType.BOOLEAN),

    //Misc. Settings
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", false, ReturnType.BOOLEAN, false),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", false, ReturnType.BOOLEAN),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", true, ReturnType.BOOLEAN),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", true, ReturnType.BOOLEAN),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", true, ReturnType.BOOLEAN),

    //Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", true, ReturnType.BOOLEAN),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", true, ReturnType.BOOLEAN),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", false, ReturnType.BOOLEAN),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", true, ReturnType.BOOLEAN),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", 500L, ReturnType.LONG),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", 22f, ReturnType.FLOAT),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", 127, ReturnType.INTEGER),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", 30f, ReturnType.FLOAT),

    //Buffer Settings
    MAX_BUFFER("revanced_pref_max_buffer_ms", 120000, ReturnType.INTEGER),
    PLAYBACK_MAX_BUFFER("revanced_pref_buffer_for_playback_ms", 2500, ReturnType.INTEGER),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER("revanced_pref_buffer_for_playback_after_rebuffer_ms", 5000, ReturnType.INTEGER),

    //ReVanced General Settings
    DEBUG("revanced_debug_enabled", false, ReturnType.BOOLEAN, true),
    USE_DARK_THEME("app_theme_dark", false, ReturnType.BOOLEAN),

    //RYD Settings
    RYD_USER_ID("ryd_userId", null, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.STRING),
    RYD_ENABLED("ryd_enabled", true, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),

    //SponsorBlock Settings
    SB_ENABLED("sb-enabled", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SHOW_TOAST_WHEN_SKIP("show-toast", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_COUNT_SKIPS("count-skips", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_UUID("uuid", "", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", 150, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_MIN_DURATION("sb-min-duration", 0F, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.FLOAT),
    SB_SEEN_GUIDELINES("sb-seen-gl", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_VOTING_ENABLED("sb-voting-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SKIPPED_SEGMENTS("sb-skipped-segments", 0, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_SKIPPED_SEGMENTS_TIME("sb-skipped-segments-time", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_IS_VIP("sb-is-vip", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_LAST_VIP_CHECK("sb-last-vip-check", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_SHOW_BROWSER_BUTTON("sb-browser-button", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_API_URL("sb-api-url", "https://sponsor.ajay.app/api/", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING);

    public static boolean shorts_playing = false;
    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;

    private Object value = null;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.YOUTUBE;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, Boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.YOUTUBE;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    static {
        load();
    }

    private static void load() {
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            Log.e("revanced: SettingsEnum", "Context returned null! Setings NOT initialized");
        } else {
            try {
                for (SettingsEnum setting : values()) {
                    Object value = setting.getDefaultValue();

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loading Setting: " + setting.name());

                    switch (setting.getReturnType()) {
                        case FLOAT:
                            value = SharedPrefHelper.getFloat(context, setting.sharedPref, setting.getPath(), (float) setting.getDefaultValue());
                            break;
                        case LONG:
                            value = SharedPrefHelper.getLong(context, setting.sharedPref, setting.getPath(), (long) setting.getDefaultValue());
                            break;
                        case BOOLEAN:
                            value = SharedPrefHelper.getBoolean(context, setting.sharedPref, setting.getPath(), (boolean) setting.getDefaultValue());
                            break;
                        case INTEGER:
                            value = SharedPrefHelper.getInt(context, setting.sharedPref, setting.getPath(), (int) setting.getDefaultValue());
                            break;
                        case STRING:
                            value = SharedPrefHelper.getString(context, setting.sharedPref, setting.getPath(), (String) setting.getDefaultValue());
                            break;
                        default:
                            LogHelper.printException(SettingsEnum.class, "Setting does not have a valid Type. Name is: " + setting.name());
                            break;
                    }
                    setting.setValue(value);

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loaded Setting: " + setting.name() + " Value: " + value);
                }
            } catch (Throwable th) {
                LogHelper.printException(SettingsEnum.class, "Error during load()!", th);
            }
        }
    }

    public static List<SettingsEnum> getAdRemovalSettings() {
        List<SettingsEnum> list = new ArrayList<>();
        for (SettingsEnum var : SettingsEnum.values()) {
            if (var.toString().startsWith("ADREMOVER")) {
                list.add(var);
            }
        }
        return list;
    }

    public void setValue(Object newValue) {
        this.value = newValue;
    }

    public void saveValue(Object newValue) {
        Context context = ReVancedUtils.getContext();
        if (context != null) {
            if (returnType == ReturnType.BOOLEAN) {
                SharedPrefHelper.saveBoolean(context, sharedPref, path, (Boolean) newValue);
            } else {
                SharedPrefHelper.saveString(context, sharedPref, path, newValue + "");
            }
            value = newValue;
        } else {
            LogHelper.printException(SettingsEnum.class, "Context on SaveValue is null!");
        }
    }

    public int getInt() {
        return (int) value;
    }

    public String getString() {
        return (String) value;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public Float getFloat() {
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public boolean shouldRebootOnChange() {
        return rebootApp;
    }
}
