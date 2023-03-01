package app.revanced.integrations.settings;

import static app.revanced.integrations.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.FLOAT;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.INTEGER;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.LONG;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.STRING;
import static app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames;
import static app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames.RETURN_YOUTUBE_DISLIKE;
import static app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK;

import android.content.Context;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", Environment.getExternalStorageDirectory().getPath() + "/Download", ReturnType.STRING),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads_enabled", true, BOOLEAN, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", "org.schabi.newpipe" /* NewPipe */, STRING),

    // Copy video URL settings
    COPY_VIDEO_URL_BUTTON_SHOWN("revanced_copy_video_url_enabled", true, BOOLEAN, true),
    COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN("revanced_copy_video_url_timestamp_enabled", true, BOOLEAN, true),

    // Video settings
    OLD_STYLE_VIDEO_QUALITY_PLAYER_SETTINGS("revanced_use_old_style_quality_settings", true, BOOLEAN),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", true, BOOLEAN),
    REMEMBER_PLAYBACK_RATE_SELECTED("revanced_remember_playback_rate_selected", true, BOOLEAN),


    // Whitelist settings
    //ToDo: Not used atm, Patch missing
    ENABLE_WHITELIST("revanced_whitelist_ads_enabled", false, BOOLEAN),

    // Ad settings
    ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", false, BOOLEAN),
    ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", "", STRING, true),
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", true, BOOLEAN, true),
    ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", true, BOOLEAN),
    ADREMOVER_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", true, BOOLEAN),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL("revanced_adremover_community_posts_removal", false, BOOLEAN),
    ADREMOVER_COMPACT_BANNER_REMOVAL("revanced_adremover_compact_banner_removal", true, BOOLEAN),
    ADREMOVER_MOVIE_REMOVAL("revanced_adremover_movie", true, BOOLEAN),
    ADREMOVER_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", true, BOOLEAN),
    ADREMOVER_SHORTS_REMOVAL("revanced_adremover_shorts", true, BOOLEAN, true),
    ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_community_guidelines", true, BOOLEAN),
    ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", true, BOOLEAN),
    ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL("revanced_adremover_channel_member_shelf_removal", true, BOOLEAN),
    ADREMOVER_EMERGENCY_BOX_REMOVAL("revanced_adremover_emergency_box_removal", true, BOOLEAN),
    ADREMOVER_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", true, BOOLEAN),
    ADREMOVER_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", true, BOOLEAN),
    ADREMOVER_PAID_CONTENT_REMOVAL("revanced_adremover_paid_content", true, BOOLEAN),
    ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", true, BOOLEAN),
    ADREMOVER_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", true, BOOLEAN),
    ADREMOVER_SELF_SPONSOR_REMOVAL("revanced_adremover_self_sponsor", true, BOOLEAN),
    ADREMOVER_CHAPTER_TEASER_REMOVAL("revanced_adremover_chapter_teaser", true, BOOLEAN),
    ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", true, BOOLEAN),
    ADREMOVER_GRAY_SEPARATOR("revanced_adremover_separator", true, BOOLEAN),
    ADREMOVER_VIEW_PRODUCTS("revanced_adremover_view_products", true, BOOLEAN),
    ADREMOVER_WEB_SEARCH_RESULTS("revanced_adremover_web_search_result", true, BOOLEAN),
    ADREMOVER_CHANNEL_BAR("revanced_hide_channel_bar", false, BOOLEAN),
    ADREMOVER_QUICK_ACTIONS("revanced_hide_quick_actions", false, BOOLEAN),
    ADREMOVER_RELATED_VIDEOS("revanced_hide_related_videos", false, BOOLEAN),
    ADREMOVER_IMAGE_SHELF("revanced_hide_image_shelf", true, BOOLEAN),

    // Action buttons
    HIDE_LIKE_BUTTON("revanced_hide_like_button", false, BOOLEAN),
    HIDE_DISLIKE_BUTTON("revanced_hide_dislike_button", false, BOOLEAN),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", false, BOOLEAN),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", false, BOOLEAN),
    HIDE_ACTION_BUTTON("revanced_hide_action_button", false, BOOLEAN),
    HIDE_SHARE_BUTTON("revanced_hide_share_button", false, BOOLEAN),

    // Layout settings
    DISABLE_STARTUP_SHORTS_PLAYER("revanced_startup_shorts_player_enabled", false, BOOLEAN),
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", false, BOOLEAN),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", false, BOOLEAN, true),
    SPOOF_APP_VERSION("revanced_spoof_app_version", false, BOOLEAN, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", false, BOOLEAN, true),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", false, BOOLEAN, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", false, BOOLEAN),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", true, BOOLEAN, true),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", true, BOOLEAN),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", false, BOOLEAN),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", true, BOOLEAN, true),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", false, BOOLEAN, true),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", true, BOOLEAN, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", false, BOOLEAN, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", false, BOOLEAN),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", true, BOOLEAN),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", true, BOOLEAN), //ToDo: Add to prefs
    HIDE_INFO_CARDS("revanced_hide_infocards", true, BOOLEAN),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", false, BOOLEAN, true),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", false, BOOLEAN, true),
    HIDE_REEL_BUTTON("revanced_hide_reel_button", true, BOOLEAN, true),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", true, BOOLEAN, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", false, BOOLEAN),
    HIDE_TIMESTAMP("revanced_hide_timestamp", false, BOOLEAN),
    HIDE_SEEKBAR("revanced_hide_seekbar", false, BOOLEAN),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", false, BOOLEAN, true),
    HIDE_BREAKING_NEWS("revanced_hide_breaking_news", true, BOOLEAN, true),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", false, BOOLEAN),
    HIDE_FLOATING_MICROPHONE_BUTTON("revanced_hide_floating_microphone_button", true, BOOLEAN, true),

    // Misc. Settings
    FIX_PLAYBACK("revanced_fix_playback", false, BOOLEAN),
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", false, BOOLEAN),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", false, BOOLEAN),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", true, BOOLEAN),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", true, BOOLEAN),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", true, BOOLEAN),
    OPEN_LINKS_DIRECTLY("revanced_uri_redirect", true, BOOLEAN, true),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", true, BOOLEAN),
    ENABLE_EXTERNAL_BROWSER("revanced_enable_external_browser", true, BOOLEAN, true),

    // Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", true, BOOLEAN),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", true, BOOLEAN),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", false, BOOLEAN),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", true, BOOLEAN),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", 500L, LONG),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", 22f, FLOAT),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", 127, INTEGER),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", 30f, FLOAT),

    // Buffer settings
    MAX_BUFFER("revanced_pref_max_buffer_ms", 120000, INTEGER),
    PLAYBACK_MAX_BUFFER("revanced_pref_buffer_for_playback_ms", 2500, INTEGER),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER("revanced_pref_buffer_for_playback_after_rebuffer_ms", 5000, INTEGER),

    // Debug settings
    DEBUG("revanced_debug_enabled", false, BOOLEAN),
    DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", false, BOOLEAN),
    DEBUG_SHOW_TOAST_ON_ERROR("revanced_debug_toast_on_error_enabled", true, BOOLEAN),

    USE_DARK_THEME("app_theme_dark", false, BOOLEAN),

    // RYD settings
    RYD_USER_ID("ryd_userId", null, RETURN_YOUTUBE_DISLIKE, STRING),
    RYD_ENABLED("ryd_enabled", true, RETURN_YOUTUBE_DISLIKE, BOOLEAN),
    RYD_SHOW_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", false, RETURN_YOUTUBE_DISLIKE, BOOLEAN),
    RYD_USE_COMPACT_LAYOUT("ryd_use_compact_layout", false, RETURN_YOUTUBE_DISLIKE, BOOLEAN),

    // SponsorBlock settings
    SB_ENABLED("sb-enabled", true, SPONSOR_BLOCK, BOOLEAN),
    SB_SHOW_TOAST_WHEN_SKIP("show-toast", true, SPONSOR_BLOCK, BOOLEAN),
    SB_COUNT_SKIPS("count-skips", true, SPONSOR_BLOCK, BOOLEAN),
    SB_UUID("uuid", "", SPONSOR_BLOCK, STRING),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", 150, SPONSOR_BLOCK, INTEGER),
    SB_MIN_DURATION("sb-min-duration", 0F, SPONSOR_BLOCK, FLOAT),
    SB_SEEN_GUIDELINES("sb-seen-gl", false, SPONSOR_BLOCK, BOOLEAN),
    SB_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", false, SPONSOR_BLOCK, BOOLEAN),
    SB_VOTING_ENABLED("sb-voting-enabled", false, SPONSOR_BLOCK, BOOLEAN),
    SB_SKIPPED_SEGMENTS("sb-skipped-segments", 0, SPONSOR_BLOCK, INTEGER),
    SB_SKIPPED_SEGMENTS_TIME("sb-skipped-segments-time", 0L, SPONSOR_BLOCK, LONG),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", true, SPONSOR_BLOCK, BOOLEAN),
    SB_IS_VIP("sb-is-vip", false, SPONSOR_BLOCK, BOOLEAN),
    SB_LAST_VIP_CHECK("sb-last-vip-check", 0L, SPONSOR_BLOCK, LONG),
    SB_SHOW_BROWSER_BUTTON("sb-browser-button", false, SPONSOR_BLOCK, BOOLEAN),
    SB_API_URL("sb-api-host-url", "https://sponsor.ajay.app", SPONSOR_BLOCK, STRING);

    private final String path;
    private final Object defaultValue;
    private final SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;

    // must be volatile, as some settings are read/write from different threads
    // of note, the object value is persistently stored using SharedPreferences (which is thread safe)
    private volatile Object value;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this(path, defaultValue, SharedPrefNames.YOUTUBE, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, boolean rebootApp) {
        this(path, defaultValue, SharedPrefNames.YOUTUBE, returnType, rebootApp);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefNames prefName, ReturnType returnType) {
        this(path, defaultValue, prefName, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefNames prefName, ReturnType returnType, boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    static {
        loadAllSettings();
    }

    private static void loadAllSettings() {
        if (ReVancedUtils.getContext() == null) {
            LogHelper.printException(() -> "SettingsEnum loaded before ReVancedUtils context was set");
            return;
        }
        for (SettingsEnum setting : values()) {
            setting.load();
        }
    }

    private void load() {
        switch (returnType) {
            case FLOAT:
                value = SharedPrefHelper.getFloat(sharedPref, path, (float) defaultValue);
                break;
            case LONG:
                value = SharedPrefHelper.getLong(sharedPref, path, (long) defaultValue);
                break;
            case BOOLEAN:
                value = SharedPrefHelper.getBoolean(sharedPref, path, (boolean) defaultValue);
                break;
            case INTEGER:
                value = SharedPrefHelper.getInt(sharedPref, path, (int) defaultValue);
                break;
            case STRING:
                value = SharedPrefHelper.getString(sharedPref, path, (String) defaultValue);
                break;
            default:
                LogHelper.printException(() -> "Setting does not have a valid Type: " + name());
                break;
        }
    }

    /**
     * Sets, but does _not_ persistently save the value.
     *
     * @see #saveValue(Object)
     */
    public void setValue(Object newValue) {
        this.value = newValue;
    }

    /**
     * Sets the value, and persistently saves it
     */
    public void saveValue(Object newValue) {
        switch (returnType) {
            case FLOAT:
                SharedPrefHelper.saveFloat(sharedPref, path, (float) newValue);
                break;
            case LONG:
                SharedPrefHelper.saveLong(sharedPref, path, (long) newValue);
                break;
            case BOOLEAN:
                SharedPrefHelper.saveBoolean(sharedPref, path, (boolean) newValue);
                break;
            case INTEGER:
                SharedPrefHelper.saveInt(sharedPref, path, (int) newValue);
                break;
            case STRING:
                SharedPrefHelper.saveString(sharedPref, path, (String) newValue);
                break;
            default:
                LogHelper.printException(() -> "Setting does not have a valid Type: " + name());
                break;
        }

        value = newValue;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public int getInt() {
        return (Integer) value;
    }

    public long getLong() {
        return (Long) value;
    }

    public float getFloat() {
        return (Float) value;
    }

    public String getString() {
        return (String) value;
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

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        STRING,
        LONG,
        FLOAT,
    }
}
