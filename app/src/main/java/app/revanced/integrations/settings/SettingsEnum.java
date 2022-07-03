package app.revanced.integrations.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {

    //ToDo: Sort these
    DEBUG_BOOLEAN("debug_revanced_enabled", false),
    MANUFACTURER_OVERRIDE_STRING("override_manufacturer", null),
    MODEL_OVERRIDE_STRING("override_model", null),
    CODEC_OVERRIDE_BOOLEAN("revanced_vp9_enabled", true),
    PREFERRED_RESOLUTION_WIFI_INTEGER("pref_video_quality_wifi", -2),
    PREFERRED_RESOLUTION_MOBILE_INTEGER("pref_video_quality_mobile", -2),
    PREFERRED_VIDEO_SPEED_FLOAT("pref_video_speed", -2.0f),
    PREFERRED_MINIMIZED_VIDEO_PREVIEW_INTEGER("pref_minimized_video_preview", -2),
    PREFERRED_AUTO_REPEAT_BOOLEAN("pref_auto_repeat", true),
    HOME_ADS_SHOWN_BOOLEAN("home_ads_enabled", false),
    VIDEO_ADS_SHOWN_BOOLEAN("video_ads_enabled", false),
    REEL_BUTTON_SHOWN_BOOLEAN("reel_button_enabled", false),
    SHORTS_BUTTON_SHOWN_BOOLEAN("shorts_button_enabled", false),
    CAST_BUTTON_SHOWN_BOOLEAN("cast_button_enabled", false),
    CREATE_BUTTON_SHOWN_BOOLEAN("revanced_create_button_enabled", false),
    INFO_CARDS_SHOWN_BOOLEAN("info_cards_enabled", false),
    BRANDING_SHOWN_BOOLEAN("branding_watermark_enabled", false),
    USE_TABLET_MINIPLAYER_BOOLEAN("tablet_miniplayer", false),
    USE_NEW_ACTIONBAR_BOOLEAN("revanced_new_actionbar", false),
    USE_DARK_THEME_BOOLEAN("app_theme_dark", false),
    USE_HDR_BRIGHTNESS_BOOLEAN("pref_hdr_autobrightness", true),
    ENABLE_SWIPE_BRIGHTNESS_BOOLEAN("pref_xfenster_brightness", true),
    ENABLE_SWIPE_VOLUME_BOOLEAN("pref_xfenster_volume", true),
    SWIPE_THRESHOLD_INTEGER("pref_xfenster_swipe_threshold", 30),
    SWIPE_PADDING_TOP_INTEGER("pref_xfenster_swipe_padding_top", 50),
    SWIPE_USE_TABLET_MODE("pref_xfenster_tablet", false),
    MAX_BUFFER_INTEGER("pref_max_buffer_ms", 120000),
    PLAYBACK_MAX_BUFFER_INTEGER("pref_buffer_for_playback_ms", 2500),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER("pref_buffer_for_playback_after_rebuffer_ms", 5000),
    OLD_STYLE_QUALITY_SETTINGS_BOOLEAN("old_style_quality_settings", true),
    TAP_SEEKING_ENABLED_BOOLEAN("revanced_enable_tap_seeking", true),

    //Settings for the adremover
    ADREMOVER_COMMUNITY_GUIDELINES_BOOLEAN("revanced_adremover_community_guidelines", true),
    ADREMOVER_AD_REMOVAL_BOOLEAN("revanced_adremover_ad_removal", true),
    ADREMOVER_COMMENTS_REMOVAL_BOOLEAN("revanced_adremover_comments_removal", false),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL_BOOLEAN("revanced_adremover_community_posts_removal", false),
    ADREMOVER_COMPACT_BANNER_REMOVAL_BOOLEAN("revanced_adremover_compact_banner_removal", false),
    ADREMOVER_EMERGENCY_BOX_REMOVAL_BOOLEAN("revanced_adremover_emergency_box_removal", true),
    ADREMOVER_INFO_PANEL_REMOVAL_BOOLEAN("revanced_adremover_info_panel", true),
    ADREMOVER_MEDICAL_PANEL_REMOVAL_BOOLEAN("revanced_adremover_medical_panel", true),
    ADREMOVER_MERCHANDISE_REMOVAL_BOOLEAN("revanced_adremover_merchandise", true),
    ADREMOVER_MOVIE_REMOVAL_BOOLEAN("revanced_adremover_movie", true),
    ADREMOVER_PAID_CONTECT_REMOVAL_BOOLEAN("revanced_adremover_paid_content", true),
    ADREMOVER_SUGGESTED_FOR_YOU_REMOVAL_BOOLEAN("revanced_adremover_suggested", true),
    ADREMOVER_FEED_SURVEY_REMOVAL_BOOLEAN("revanced_adremover_feed_survey", false),
    ADREMOVER_SHORTS_SHELF_BOOLEAN("revanced_adremover_shorts_shelf", true);

    private final String path;
    private final Object defaultValue;

    private Object value = null;
    private static boolean loaded = false;

    SettingsEnum(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public static void loadSettings() {
        if (loaded) return;

        Context context = ReVancedUtils.getContext();
        if (context != null) {
            for (SettingsEnum setting : values()) {
                Object value = null;
                if (setting.name().endsWith("BOOLEAN")) {
                    value = SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath());
                } else if (setting.name().endsWith("INTEGER")) {
                    value = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath());
                } else if (setting.name().endsWith("STRING")) {
                    value = SharedPrefHelper.getString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath());
                } else if (setting.name().endsWith("LONG")) {
                    value = SharedPrefHelper.getLong(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath());
                } else if (setting.name().endsWith(("FLOAT"))) {
                    value = SharedPrefHelper.getFloat(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath());
                } else {
                    LogHelper.printException(SettingsEnum.class, "Setting does not end with a valid Type. Name is: " + setting.name());
                    continue;
                }


                if (value == null) value = setting.getDefaultValue();
                setting.setValue(value);
            }
            loaded = true;
        }
    }

    public static boolean isLoaded() {
        return loaded;
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
        loadSettings();
        Context context = ReVancedUtils.getContext();
        if (context != null) {
            if (name().endsWith("BOOLEAN")) {
                SharedPrefHelper.saveBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, getPath(), (Boolean) newValue);
            } else if (name().endsWith("INTEGER")) {
                SharedPrefHelper.saveInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, getPath(), (int) newValue);
            } else if (name().endsWith("STRING")) {
                SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, getPath(), (String) newValue);
            } else if (name().endsWith("LONG")) {
                SharedPrefHelper.saveLong(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, getPath(), (Long) newValue);
            } else if (name().endsWith(("FLOAT"))) {
                SharedPrefHelper.saveFloat(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, getPath(), (Float) newValue);
            } else {
                LogHelper.printException(SettingsEnum.class, "Setting does not end with a valid Type. Name is: " + name());
            }
            value = newValue;
        } else {
            LogHelper.printException(SettingsEnum.class, "Context on SaveValue is null!");
        }
    }

    public int getInt() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1;
        LogHelper.debug(SettingsEnum.class, "Variable " + name() + " is " + value);
        return (int) value;
    }

    public String getString() {
        SettingsEnum.loadSettings();
        LogHelper.debug(SettingsEnum.class, "Variable " + name() + " is " + value);
        return (String) value;
    }

    public boolean getBoolean() {
        SettingsEnum.loadSettings();
        //LogHelper.debug("SettingsEnum", "Variable " + name() + " is " + value);
        return (Boolean) value;
    }

    public Long getLong() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1L;
        LogHelper.debug(SettingsEnum.class, "Variable " + name() + " is " + value);
        return (Long) value;
    }

    public Float getFloat() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1.0f;
        LogHelper.debug(SettingsEnum.class, "Variable " + name() + " is " + value);
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

}
