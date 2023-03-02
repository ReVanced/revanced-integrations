package app.revanced.integrations.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.FLOAT;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.INTEGER;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.LONG;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.STRING;

import app.revanced.integrations.utils.SharedPrefCategory;
import static app.revanced.integrations.utils.SharedPrefCategory.RETURN_YOUTUBE_DISLIKE;
import static app.revanced.integrations.utils.SharedPrefCategory.SPONSOR_BLOCK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", Environment.getExternalStorageDirectory().getPath() + "/Download", ReturnType.STRING),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads_enabled", BOOLEAN, TRUE, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", STRING, "org.schabi.newpipe" /* NewPipe */, parents(DOWNLOADS_BUTTON_SHOWN)),

    // Copy video URL settings
    COPY_VIDEO_URL_BUTTON_SHOWN("revanced_copy_video_url_enabled", BOOLEAN, TRUE, true),
    COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN("revanced_copy_video_url_timestamp_enabled", BOOLEAN, TRUE, true),

    // Video settings
    OLD_STYLE_VIDEO_QUALITY_PLAYER_SETTINGS("revanced_use_old_style_quality_settings", BOOLEAN, TRUE),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", BOOLEAN, TRUE),
    REMEMBER_PLAYBACK_RATE_SELECTED("revanced_remember_playback_rate_selected", BOOLEAN, TRUE),

    // Whitelist settings
    //TODO: Patch missing
    //ENABLE_WHITELIST("revanced_whitelist_ads_enabled", BOOLEAN, FALSE),

    // Ad settings
    ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", BOOLEAN, TRUE),
    ADREMOVER_CHANNEL_BAR("revanced_hide_channel_bar", BOOLEAN, FALSE),
    ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL("revanced_adremover_channel_member_shelf_removal", BOOLEAN, TRUE),
    ADREMOVER_CHAPTER_TEASER_REMOVAL("revanced_adremover_chapter_teaser", BOOLEAN, TRUE),
    ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_community_guidelines", BOOLEAN, TRUE),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL("revanced_adremover_community_posts_removal", BOOLEAN, FALSE),
    ADREMOVER_COMPACT_BANNER_REMOVAL("revanced_adremover_compact_banner_removal", BOOLEAN, TRUE),
    ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", BOOLEAN, FALSE),
    ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", STRING, "", true),
    ADREMOVER_EMERGENCY_BOX_REMOVAL("revanced_adremover_emergency_box_removal", BOOLEAN, TRUE),
    ADREMOVER_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", BOOLEAN, TRUE),
    ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", BOOLEAN, TRUE),
    ADREMOVER_GRAY_SEPARATOR("revanced_adremover_separator", BOOLEAN, TRUE),
    ADREMOVER_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", BOOLEAN, TRUE),
    ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", BOOLEAN, TRUE),
    ADREMOVER_IMAGE_SHELF("revanced_hide_image_shelf", BOOLEAN, TRUE),
    ADREMOVER_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", BOOLEAN, TRUE),
    ADREMOVER_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", BOOLEAN, TRUE),
    ADREMOVER_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", BOOLEAN, TRUE),
    ADREMOVER_MOVIE_REMOVAL("revanced_adremover_movie", BOOLEAN, TRUE),
    ADREMOVER_PAID_CONTENT_REMOVAL("revanced_adremover_paid_content", BOOLEAN, TRUE),
    ADREMOVER_QUICK_ACTIONS("revanced_hide_quick_actions", BOOLEAN, FALSE),
    ADREMOVER_RELATED_VIDEOS("revanced_hide_related_videos", BOOLEAN, FALSE),
    ADREMOVER_SELF_SPONSOR_REMOVAL("revanced_adremover_self_sponsor", BOOLEAN, TRUE),
    ADREMOVER_SHORTS_REMOVAL("revanced_adremover_shorts", BOOLEAN, TRUE, true),
    ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", BOOLEAN, TRUE),
    ADREMOVER_VIEW_PRODUCTS("revanced_adremover_view_products", BOOLEAN, TRUE),
    ADREMOVER_WEB_SEARCH_RESULTS("revanced_adremover_web_search_result", BOOLEAN, TRUE),
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", BOOLEAN, TRUE, true),

    // Action buttons
    HIDE_ACTION_BUTTON("revanced_hide_action_button", BOOLEAN, FALSE),
    HIDE_DISLIKE_BUTTON("revanced_hide_dislike_button", BOOLEAN, FALSE),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", BOOLEAN, FALSE),
    HIDE_LIKE_BUTTON("revanced_hide_like_button", BOOLEAN, FALSE),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", BOOLEAN, FALSE),
    HIDE_SHARE_BUTTON("revanced_hide_share_button", BOOLEAN, FALSE),

    // Layout settings
    DISABLE_STARTUP_SHORTS_PLAYER("revanced_startup_shorts_player_enabled", BOOLEAN, FALSE),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", BOOLEAN, FALSE, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", BOOLEAN, FALSE),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", BOOLEAN, TRUE, true),
    HIDE_BREAKING_NEWS("revanced_hide_breaking_news", BOOLEAN, TRUE, true),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", BOOLEAN, FALSE),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", BOOLEAN, TRUE, true),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", BOOLEAN, FALSE, true),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", BOOLEAN, TRUE, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", BOOLEAN, FALSE, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", BOOLEAN, FALSE),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", BOOLEAN, TRUE),
    HIDE_FLOATING_MICROPHONE_BUTTON("revanced_hide_floating_microphone_button", BOOLEAN, TRUE, true),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", BOOLEAN, TRUE), //ToDo: Add to prefs
    HIDE_INFO_CARDS("revanced_hide_infocards", BOOLEAN, TRUE),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", BOOLEAN, FALSE, true),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", BOOLEAN, FALSE),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", BOOLEAN, FALSE, true),
    HIDE_REEL_BUTTON("revanced_hide_reel_button", BOOLEAN, TRUE, true),
    HIDE_SEEKBAR("revanced_hide_seekbar", BOOLEAN, FALSE),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", BOOLEAN, TRUE, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", BOOLEAN, FALSE),
    HIDE_TIMESTAMP("revanced_hide_timestamp", BOOLEAN, FALSE),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", BOOLEAN, TRUE),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", BOOLEAN, FALSE, true),
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", BOOLEAN, FALSE),
    SPOOF_APP_VERSION("revanced_spoof_app_version", BOOLEAN, FALSE, true),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", BOOLEAN, FALSE, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", BOOLEAN, FALSE, true),

    // Misc. Settings
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", BOOLEAN, FALSE),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", BOOLEAN, TRUE),
    ENABLE_EXTERNAL_BROWSER("revanced_enable_external_browser", BOOLEAN, TRUE, true),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", BOOLEAN, TRUE),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", BOOLEAN, FALSE),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", BOOLEAN, TRUE),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", BOOLEAN, TRUE),

    // Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", BOOLEAN, TRUE),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", BOOLEAN, TRUE),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", BOOLEAN, FALSE),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", BOOLEAN, TRUE,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", FLOAT, 30f,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", INTEGER, 127,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", FLOAT, 22f,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", LONG, 500L,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),

    // Buffer settings
    MAX_BUFFER("revanced_pref_max_buffer_ms", INTEGER, 120000),
    MAX_BUFFER_PLAYBACK("revanced_pref_buffer_for_playback_ms", INTEGER, 2500),
    MAX_BUFFER_PLAYBACK_AFTER_REBUFFER("revanced_pref_buffer_for_playback_after_rebuffer_ms", INTEGER, 5000),

    // Debug settings
    DEBUG("revanced_debug_enabled", BOOLEAN, FALSE),
    DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", BOOLEAN, FALSE, parents(DEBUG)),
    DEBUG_SHOW_TOAST_ON_ERROR("revanced_debug_toast_on_error_enabled", BOOLEAN, TRUE),

    // ReturnYoutubeDislike settings
    RYD_ENABLED("ryd_enabled", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE),
    RYD_USER_ID("ryd_userId", STRING, "", RETURN_YOUTUBE_DISLIKE),
    RYD_SHOW_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),
    RYD_USE_COMPACT_LAYOUT("ryd_use_compact_layout", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),

    // SponsorBlock settings
    SB_ENABLED("sb-enabled", BOOLEAN, TRUE, SPONSOR_BLOCK),
    SB_API_URL("sb-api-host-url", STRING, "https://sponsor.ajay.app", SPONSOR_BLOCK),
    SB_UUID("uuid", STRING, "", SPONSOR_BLOCK),
    SB_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_VOTING_ENABLED("sb-voting-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SHOW_TOAST_WHEN_SKIP("show-toast", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_COUNT_SKIPS("count-skips", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", INTEGER, 150, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_MIN_DURATION("sb-min-duration", FLOAT, 0F, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SEEN_GUIDELINES("sb-seen-gl", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS("sb-skipped-segments", INTEGER, 0, SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS_TIME("sb-skipped-segments-time", LONG, 0L, SPONSOR_BLOCK),
    SB_IS_VIP("sb-is-vip", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_LAST_VIP_CHECK("sb-last-vip-check", LONG, 0L, SPONSOR_BLOCK);

    private static SettingsEnum[] parents(SettingsEnum ... parents) {
        return parents;
    }

    @NonNull
    public final String path;
    @NonNull
    public final Object defaultValue;
    @NonNull
    public final SharedPrefCategory sharedPref;
    @NonNull
    public final ReturnType returnType;
    public final boolean rebootApp;
    /**
     * Set of boolean parent settings.
     * If any of the parents are enabled, then this setting is available to configure.
     *
     * For example: {@link #DEBUG_STACKTRACE} is non-functional and cannot be configured,
     * unless it's parent {@link #DEBUG} is enabled.
     *
     * Used only for items that show up in the the ReVanced Settings UI.
     */
    @Nullable
    private final SettingsEnum[] parents;

    // must be volatile, as some settings are read/write from different threads
    // of note, the object value is persistently stored using SharedPreferences (which is thread safe)
    @NonNull
    private volatile Object value;

    SettingsEnum(String path, ReturnType returnType, Object defaultValue) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, parents);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SharedPrefCategory prefName) {
        this(path, returnType, defaultValue, prefName, false, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SharedPrefCategory prefName, boolean rebootApp) {
        this(path, returnType, defaultValue, prefName, rebootApp, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SharedPrefCategory prefName, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, prefName, false, parents);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SharedPrefCategory prefName, boolean rebootApp, @Nullable SettingsEnum[]  parents) {
        this.path = Objects.requireNonNull(path);
        this.returnType = Objects.requireNonNull(returnType);
        this.value = this.defaultValue = Objects.requireNonNull(defaultValue);
        this.sharedPref = Objects.requireNonNull(prefName);
        this.rebootApp = rebootApp;
        this.parents = parents;
        if (parents != null) {
            for (SettingsEnum parent : parents) {
                if (parent.returnType != ReturnType.BOOLEAN) {
                    throw new IllegalArgumentException(" parent must be Boolean type: " + parent);
                }
            }
        }
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
                throw new IllegalStateException(name());
        }
    }

    /**
     * Sets, but does _not_ persistently save the value
     *
     * @see #saveValue(Object)
     */
    public void setValue(@NonNull Object newValue) {
        this.value = Objects.requireNonNull(newValue);
    }

    /**
     * Sets the value, and persistently saves it
     */
    public void saveValue(@NonNull Object newValue) {
        Objects.requireNonNull(newValue);
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
                throw new IllegalStateException(name());
        }
        value = newValue;
    }

    /**
     * @return if this setting can be configured and used.  Not to be confused with {@link #getBoolean()}
     */
    public boolean isAvailable() {
        if (parents == null) {
            return true;
        }
        for (SettingsEnum parent : parents) {
            if (parent.getBoolean()) return true;
        }
        return false;
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

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        STRING,
        LONG,
        FLOAT,
    }
}