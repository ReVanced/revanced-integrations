package app.revanced.integrations.settings;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", Environment.getExternalStorageDirectory().getPath() + "/Download", ReturnType.STRING),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads_enabled", true, ReturnType.BOOLEAN, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", "org.schabi.newpipe" /* NewPipe */, ReturnType.STRING),

    // Copy video URL settings
    COPY_VIDEO_URL_BUTTON_SHOWN("revanced_copy_video_url_enabled", true, ReturnType.BOOLEAN, true),
    COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN("revanced_copy_video_url_timestamp_enabled", true, ReturnType.BOOLEAN, true),

    // Video settings
    OLD_STYLE_VIDEO_QUALITY_PLAYER_SETTINGS("revanced_use_old_style_quality_settings", true, ReturnType.BOOLEAN),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", true, ReturnType.BOOLEAN),
    REMEMBER_PLAYBACK_SPEED_LAST_SELECTED("revanced_remember_playback_speed_last_selected", true, ReturnType.BOOLEAN),
    REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE("revanced_remember_playback_speed_last_selected_value", 1.0f, ReturnType.FLOAT),

    // TODO: Unused currently
    // Whitelist settings
    ENABLE_WHITELIST("revanced_whitelist_ads_enabled", false, ReturnType.BOOLEAN),

    // Ad settings
    ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", false, ReturnType.BOOLEAN),
    ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", "", ReturnType.STRING, true),
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", true, ReturnType.BOOLEAN, true),
    ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", true, ReturnType.BOOLEAN),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL("revanced_adremover_community_posts_removal", false, ReturnType.BOOLEAN),
    ADREMOVER_COMPACT_BANNER_REMOVAL("revanced_adremover_compact_banner_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_MOVIE_REMOVAL("revanced_adremover_movie", true, ReturnType.BOOLEAN),
    ADREMOVER_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", true, ReturnType.BOOLEAN),
    ADREMOVER_SHORTS_REMOVAL("revanced_adremover_shorts", true, ReturnType.BOOLEAN, true),
    ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_community_guidelines", true, ReturnType.BOOLEAN),
    ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL("revanced_adremover_channel_member_shelf_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_EMERGENCY_BOX_REMOVAL("revanced_adremover_emergency_box_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", true, ReturnType.BOOLEAN),
    ADREMOVER_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", true, ReturnType.BOOLEAN),
    ADREMOVER_PAID_CONTENT_REMOVAL("revanced_adremover_paid_content", true, ReturnType.BOOLEAN),
    ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", true, ReturnType.BOOLEAN),
    ADREMOVER_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", true, ReturnType.BOOLEAN),
    ADREMOVER_SELF_SPONSOR_REMOVAL("revanced_adremover_self_sponsor", true, ReturnType.BOOLEAN),
    ADREMOVER_CHAPTER_TEASER_REMOVAL("revanced_adremover_chapter_teaser", true, ReturnType.BOOLEAN),
    ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", true, ReturnType.BOOLEAN),
    ADREMOVER_GRAY_SEPARATOR("revanced_adremover_separator", true, ReturnType.BOOLEAN),
    ADREMOVER_VIEW_PRODUCTS("revanced_adremover_view_products", true, ReturnType.BOOLEAN),
    ADREMOVER_WEB_SEARCH_RESULTS("revanced_adremover_web_search_result", true, ReturnType.BOOLEAN),
    ADREMOVER_CHANNEL_BAR("revanced_hide_channel_bar", false, ReturnType.BOOLEAN),
    ADREMOVER_QUICK_ACTIONS("revanced_hide_quick_actions", false, ReturnType.BOOLEAN),
    ADREMOVER_RELATED_VIDEOS("revanced_hide_related_videos", false, ReturnType.BOOLEAN),
    ADREMOVER_IMAGE_SHELF("revanced_hide_image_shelf", true, ReturnType.BOOLEAN),

    // Action buttons
    HIDE_LIKE_BUTTON("revanced_hide_like_button", false, ReturnType.BOOLEAN),
    HIDE_DISLIKE_BUTTON("revanced_hide_dislike_button", false, ReturnType.BOOLEAN),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", false, ReturnType.BOOLEAN),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", false, ReturnType.BOOLEAN),
    HIDE_ACTION_BUTTON("revanced_hide_action_button", false, ReturnType.BOOLEAN),
    HIDE_SHARE_BUTTON("revanced_hide_share_button", false, ReturnType.BOOLEAN),

    // Layout settings
    DISABLE_STARTUP_SHORTS_PLAYER("revanced_startup_shorts_player_enabled", false, ReturnType.BOOLEAN),
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", false, ReturnType.BOOLEAN),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", false, ReturnType.BOOLEAN, true),
    SPOOF_APP_VERSION("revanced_spoof_app_version", false, ReturnType.BOOLEAN, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", false, ReturnType.BOOLEAN, true),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", false, ReturnType.BOOLEAN, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", false, ReturnType.BOOLEAN),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", true, ReturnType.BOOLEAN, true),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", true, ReturnType.BOOLEAN),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", false, ReturnType.BOOLEAN),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", true, ReturnType.BOOLEAN, true),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", false, ReturnType.BOOLEAN, true),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", true, ReturnType.BOOLEAN, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", false, ReturnType.BOOLEAN, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", false, ReturnType.BOOLEAN),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", true, ReturnType.BOOLEAN),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", true, ReturnType.BOOLEAN), //ToDo: Add to prefs
    HIDE_INFO_CARDS("revanced_hide_infocards", true, ReturnType.BOOLEAN),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", false, ReturnType.BOOLEAN, true),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", false, ReturnType.BOOLEAN, true),
    HIDE_REEL_BUTTON("revanced_hide_reel_button", true, ReturnType.BOOLEAN, true),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", true, ReturnType.BOOLEAN, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", false, ReturnType.BOOLEAN),
    HIDE_TIMESTAMP("revanced_hide_timestamp", false, ReturnType.BOOLEAN),
    HIDE_SEEKBAR("revanced_hide_seekbar", false, ReturnType.BOOLEAN),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", false, ReturnType.BOOLEAN, true),
    HIDE_BREAKING_NEWS("revanced_hide_breaking_news", true, ReturnType.BOOLEAN, true),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", false, ReturnType.BOOLEAN),
    HIDE_FLOATING_MICROPHONE_BUTTON("revanced_hide_floating_microphone_button", true, ReturnType.BOOLEAN, true),

    // Misc. Settings
    SIGNATURE_SPOOFING("revanced_spoof_signature_verification", false, ReturnType.BOOLEAN),
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", false, ReturnType.BOOLEAN),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", false, ReturnType.BOOLEAN),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", true, ReturnType.BOOLEAN),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", true, ReturnType.BOOLEAN),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", true, ReturnType.BOOLEAN),
    OPEN_LINKS_DIRECTLY("revanced_uri_redirect", true, ReturnType.BOOLEAN, true),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", true, ReturnType.BOOLEAN),
    ENABLE_EXTERNAL_BROWSER("revanced_enable_external_browser", true, ReturnType.BOOLEAN, true),

    // Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", true, ReturnType.BOOLEAN),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", true, ReturnType.BOOLEAN),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", false, ReturnType.BOOLEAN),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", true, ReturnType.BOOLEAN),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", 500L, ReturnType.LONG),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", 22f, ReturnType.FLOAT),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", 127, ReturnType.INTEGER),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", 30f, ReturnType.FLOAT),

    // Debug settings
    DEBUG("revanced_debug_enabled", false, ReturnType.BOOLEAN),
    DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", false, ReturnType.BOOLEAN),
    DEBUG_SHOW_TOAST_ON_ERROR("revanced_debug_toast_on_error_enabled", true, ReturnType.BOOLEAN),

    USE_DARK_THEME("app_theme_dark", false, ReturnType.BOOLEAN),

    // RYD settings
    RYD_USER_ID("ryd_userId", null, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.STRING),
    RYD_ENABLED("ryd_enabled", true, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),
    RYD_SHOW_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", false, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),
    RYD_USE_COMPACT_LAYOUT("ryd_use_compact_layout", false, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),

    // SponsorBlock settings
    SB_ENABLED("sb-enabled", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_VOTING_ENABLED("sb-voting-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_CREATE_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_USE_COMPACT_SKIPBUTTON("sb-use-compact-skip-button", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SHOW_TOAST_WHEN_SKIP("show-toast", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_COUNT_SKIPS("count-skips", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_UUID("uuid", "", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", 150, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_MIN_DURATION("sb-min-duration", 0F, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.FLOAT),
    SB_SEEN_GUIDELINES("sb-seen-gl", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SKIPPED_SEGMENTS_NUMBER_SKIPPED("sb-skipped-segments", 0, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_SKIPPED_SEGMENTS_TIME_SAVED("sb-skipped-segments-time", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_IS_VIP("sb-is-vip", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_LAST_VIP_CHECK("sb-last-vip-check", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_API_URL("sb-api-host-url", "https://sponsor.ajay.app", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING);

    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;

    // must be volatile, as some settings are read/write from different threads
    // of note, the object value is persistently stored using SharedPreferences (which is thread safe)
    private volatile Object value;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this(path, defaultValue, SharedPrefHelper.SharedPrefNames.YOUTUBE, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, boolean rebootApp) {
        this(path, defaultValue, SharedPrefHelper.SharedPrefNames.YOUTUBE, returnType, rebootApp);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType) {
        this(path, defaultValue, prefName, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType, boolean rebootApp) {
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
}
