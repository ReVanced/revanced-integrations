package app.revanced.integrations.settings;

import android.content.Context;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {

    DEBUG_BOOLEAN("debug_xfile_enabled", false),
    MANUFACTURER_OVERRIDE_STRING("override_manufacturer", null),
    MODEL_OVERRIDE_STRING("override_model", null),
    CODEC_OVERRIDE_BOOLEAN("override_resolution_xfile_enabled", false),
    PREFERRED_RESOLUTION_WIFI_INTEGER("pref_video_quality_wifi", -2),
    PREFERRED_RESOLUTION_MOBILE_INTEGER("pref_video_quality_mobile", -2),
    PREFERRED_VIDEO_SPEED_FLOAT("pref_video_speed", -2.0f),
    PREFERRED_AUTO_CAPTIONS_BOOLEAN("pref_auto_captions", false),
    PREFERRED_MINIMIZED_VIDEO_PREVIEW_INTEGER("pref_minimized_video_preview", -2),
    PREFERRED_AUTO_REPEAT_BOOLEAN("pref_auto_repeat", false),
    HOME_ADS_SHOWN_BOOLEAN("home_ads_enabled", false),
    VIDEO_ADS_SHOWN_BOOLEAN("video_ads_enabled", false),
    REEL_BUTTON_SHOWN_BOOLEAN("reel_button_enabled", false),
    SHORTS_BUTTON_SHOWN_BOOLEAN("shorts_button_enabled", false),
    CAST_BUTTON_SHOWN_BOOLEAN("cast_button_enabled", false),
    CREATE_BUTTON_SHOWN_BOOLEAN("xfile_create_button_hidden", true),
    SUGGESTIONS_SHOWN_BOOLEAN("info_card_suggestions_enabled", true),
    INFO_CARDS_SHOWN_BOOLEAN("info_cards_enabled", true),
    BRANDING_SHOWN_BOOLEAN("branding_watermark_enabled", true),
    USE_TABLET_MINIPLAYER_BOOLEAN("tablet_miniplayer", false),
    CHANGE_COMMENT_LOCATION_BOOLEAN("comments_location", false),
    USE_NEW_ACTIONBAR_BOOLEAN("xfile_new_actionbar", false),
    USE_VERTICAL_ZOOM_TO_FIT_BOOLEAN("xfile_zoom_to_fit_vertical", false),
    USE_DARK_THEME_BOOLEAN("app_theme_dark", false),
    ACCESSIBILITY_SEEK_BOOLEAN("xfile_accessibility_seek_buttons", false),
    USE_HDR_BRIGHTNESS_BOOLEAN("pref_hdr_autobrightness", false),
    ENABLE_SWIPE_BRIGHTNESS_BOOLEAN("pref_xfenster_brightness", false),
    ENABLE_SWIPE_VOLUME_BOOLEAN("pref_xfenster_volume", false),
    SWIPE_THRESHOLD_INTEGER("pref_xfenster_swipe_threshold", 30),
    SWIPE_PADDING_TOP_INTEGER("pref_xfenster_swipe_padding_top", 50),
    MAX_BUFFER_INTEGER("pref_max_buffer_ms", 120000),
    PLAYBACK_MAX_BUFFER_INTEGER("pref_buffer_for_playback_ms", 2500),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER("pref_buffer_for_playback_after_rebuffer_ms", 5000),
    OLD_STYLE_QUALITY_SETTINGS_BOOLEAN("old_style_quality_settings", true),
    OLD_LAYOUT_XFILE_ENABLED_BOOLEAN("old_layout_xfile_enabled", false),
    ;

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

        Context context;
        if ((context = YouTubeTikTokRoot_Application.getAppContext()) != null) {
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
                    LogHelper.printException("SettingsEnum", "Setting does not end with a valid Type. Name is: " + setting.name());
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

    public void setValue(Object newValue) {
        this.value = newValue;
    }

    public void saveValue(Object newValue) {
        loadSettings();
        Context context;
        if ((context = YouTubeTikTokRoot_Application.getAppContext()) != null) {
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
                LogHelper.printException("SettingsEnum", "Setting does not end with a valid Type. Name is: " + name());
            }
        } else {
            LogHelper.printException("SettingsEnum", "Context on SaveValue is null!");
        }
    }

    public int getInt() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1;
        LogHelper.debug("SettingsEnum", "Variable " + name() + " is " + value);
        return (int) value;
    }

    public String getString() {
        SettingsEnum.loadSettings();
        LogHelper.debug("SettingsEnum", "Variable " + name() + " is " + value);
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
        LogHelper.debug("SettingsEnum", "Variable " + name() + " is " + value);
        return (Long) value;
    }

    public Float getFloat() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1.0f;
        LogHelper.debug("SettingsEnum", "Variable " + name() + " is " + value);
        return (Float) value;
    }

    private Object getDefaultValue() {
        return defaultValue;
    }

    private String getPath() {
        return path;
    }

}
