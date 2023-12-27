package app.revanced.integrations.youtube.settings;

import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.shared.Logger;

import java.util.LinkedList;
import java.util.List;

import static app.revanced.integrations.shared.settings.Setting.*;
import static app.revanced.integrations.shared.settings.Setting.ReturnType.*;
import static app.revanced.integrations.youtube.settings.SharedPrefCategory.RETURN_YOUTUBE_DISLIKE;
import static app.revanced.integrations.youtube.settings.SharedPrefCategory.SPONSOR_BLOCK;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/** @noinspection unused*/
public class Settings {
    private static final List<Setting> SETTINGS = new LinkedList<>();

    // External downloader
    public static final Setting EXTERNAL_DOWNLOADER = new Setting("revanced_external_downloader", BOOLEAN, FALSE);
    public static final Setting EXTERNAL_DOWNLOADER_PACKAGE_NAME = new Setting("revanced_external_downloader_name", STRING,
            "org.schabi.newpipe" /* NewPipe */, getParents(EXTERNAL_DOWNLOADER));

    // Copy video URL
    public static final Setting COPY_VIDEO_URL = new Setting("revanced_copy_video_url", BOOLEAN, FALSE);
    public static final Setting COPY_VIDEO_URL_TIMESTAMP = new Setting("revanced_copy_video_url_timestamp", BOOLEAN, TRUE);

    // Video
    public static final Setting HDR_AUTO_BRIGHTNESS = new Setting("revanced_hdr_auto_brightness", BOOLEAN, TRUE);
    /** @noinspection DeprecatedIsStillUsed*/
    @Deprecated
    public static final Setting SHOW_OLD_VIDEO_QUALITY_MENU = new Setting("revanced_show_old_video_quality_menu", BOOLEAN, TRUE);
    public static final Setting RESTORE_OLD_VIDEO_QUALITY_MENU = new Setting("revanced_restore_old_video_quality_menu", BOOLEAN, TRUE);
    public static final Setting REMEMBER_VIDEO_QUALITY_LAST_SELECTED = new Setting("revanced_remember_video_quality_last_selected", BOOLEAN, TRUE);
    public static final Setting VIDEO_QUALITY_DEFAULT_WIFI = new Setting("revanced_video_quality_default_wifi", INTEGER, -2);
    public static final Setting VIDEO_QUALITY_DEFAULT_MOBILE = new Setting("revanced_video_quality_default_mobile", INTEGER, -2);
    public static final Setting REMEMBER_PLAYBACK_SPEED_LAST_SELECTED = new Setting("revanced_remember_playback_speed_last_selected", BOOLEAN, TRUE);
    public static final Setting PLAYBACK_SPEED_DEFAULT = new Setting("revanced_playback_speed_default", FLOAT, 1.0f);
    public static final Setting CUSTOM_PLAYBACK_SPEEDS = new Setting("revanced_custom_playback_speeds", STRING,
            "0.25\n0.5\n0.75\n0.9\n0.95\n1.0\n1.05\n1.1\n1.25\n1.5\n1.75\n2.0\n3.0\n4.0\n5.0", true);

    // Ads
    public static final Setting HIDE_FULLSCREEN_ADS = new Setting("revanced_hide_fullscreen_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_BUTTONED_ADS = new Setting("revanced_hide_buttoned_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_GENERAL_ADS = new Setting("revanced_hide_general_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_GET_PREMIUM = new Setting("revanced_hide_get_premium", BOOLEAN, TRUE);
    public static final Setting HIDE_HIDE_LATEST_POSTS = new Setting("revanced_hide_latest_posts_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_MERCHANDISE_BANNERS = new Setting("revanced_hide_merchandise_banners", BOOLEAN, TRUE);
    public static final Setting HIDE_PAID_CONTENT = new Setting("revanced_hide_paid_content_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_PRODUCTS_BANNER = new Setting("revanced_hide_products_banner", BOOLEAN, TRUE);
    public static final Setting HIDE_SHOPPING_LINKS = new Setting("revanced_hide_shopping_links", BOOLEAN, TRUE);
    public static final Setting HIDE_SELF_SPONSOR = new Setting("revanced_hide_self_sponsor_ads", BOOLEAN, TRUE);
    public static final Setting HIDE_VIDEO_ADS = new Setting("revanced_hide_video_ads", BOOLEAN, TRUE, true);
    public static final Setting HIDE_WEB_SEARCH_RESULTS = new Setting("revanced_hide_web_search_results", BOOLEAN, TRUE);
    // Layout
    public static final Setting ALT_THUMBNAIL_STILLS = new Setting("revanced_alt_thumbnail_stills", BOOLEAN, FALSE);
    public static final Setting ALT_THUMBNAIL_STILLS_TIME = new Setting("revanced_alt_thumbnail_stills_time", INTEGER, 2, getParents(ALT_THUMBNAIL_STILLS));
    public static final Setting ALT_THUMBNAIL_STILLS_FAST = new Setting("revanced_alt_thumbnail_stills_fast", BOOLEAN, FALSE, getParents(ALT_THUMBNAIL_STILLS));
    public static final Setting ALT_THUMBNAIL_DEARROW = new Setting("revanced_alt_thumbnail_dearrow", BOOLEAN, false);
    public static final Setting ALT_THUMBNAIL_DEARROW_API_URL = new Setting("revanced_alt_thumbnail_dearrow_api_url", STRING,
            "https://dearrow-thumb.ajay.app/api/v1/getThumbnail", true, getParents(ALT_THUMBNAIL_DEARROW));
    public static final Setting ALT_THUMBNAIL_DEARROW_CONNECTION_TOAST = new Setting("revanced_alt_thumbnail_dearrow_connection_toast", BOOLEAN, TRUE, getParents(ALT_THUMBNAIL_DEARROW));
    public static final Setting CUSTOM_FILTER = new Setting("revanced_custom_filter", BOOLEAN, FALSE);
    public static final Setting CUSTOM_FILTER_STRINGS = new Setting("revanced_custom_filter_strings", STRING, "", true, getParents(CUSTOM_FILTER));
    public static final Setting DISABLE_FULLSCREEN_AMBIENT_MODE = new Setting("revanced_disable_fullscreen_ambient_mode", BOOLEAN, TRUE, true);
    public static final Setting DISABLE_RESUMING_SHORTS_PLAYER = new Setting("revanced_disable_resuming_shorts_player", BOOLEAN, FALSE);
    public static final Setting DISABLE_ROLLING_NUMBER_ANIMATIONS = new Setting("revanced_disable_rolling_number_animations", BOOLEAN, FALSE);
    public static final Setting DISABLE_SUGGESTED_VIDEO_END_SCREEN = new Setting("revanced_disable_suggested_video_end_screen", BOOLEAN, TRUE);
    public static final Setting GRADIENT_LOADING_SCREEN = new Setting("revanced_gradient_loading_screen", BOOLEAN, FALSE);
    public static final Setting HIDE_ALBUM_CARDS = new Setting("revanced_hide_album_cards", BOOLEAN, FALSE, true);
    public static final Setting HIDE_ARTIST_CARDS = new Setting("revanced_hide_artist_cards", BOOLEAN, FALSE);
    public static final Setting HIDE_AUTOPLAY_BUTTON = new Setting("revanced_hide_autoplay_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_BREAKING_NEWS = new Setting("revanced_hide_breaking_news", BOOLEAN, TRUE, true);
    public static final Setting HIDE_CAPTIONS_BUTTON = new Setting("revanced_hide_captions_button", BOOLEAN, FALSE);
    public static final Setting HIDE_CAST_BUTTON = new Setting("revanced_hide_cast_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_CHANNEL_BAR = new Setting("revanced_hide_channel_bar", BOOLEAN, FALSE);
    public static final Setting HIDE_CHANNEL_MEMBER_SHELF = new Setting("revanced_hide_channel_member_shelf", BOOLEAN, TRUE);
    public static final Setting HIDE_CHIPS_SHELF = new Setting("revanced_hide_chips_shelf", BOOLEAN, TRUE);
    public static final Setting HIDE_COMMENTS_SECTION = new Setting("revanced_hide_comments_section", BOOLEAN, FALSE, true);
    public static final Setting HIDE_COMMUNITY_GUIDELINES = new Setting("revanced_hide_community_guidelines", BOOLEAN, TRUE);
    public static final Setting HIDE_COMMUNITY_POSTS = new Setting("revanced_hide_community_posts", BOOLEAN, FALSE);
    public static final Setting HIDE_COMPACT_BANNER = new Setting("revanced_hide_compact_banner", BOOLEAN, TRUE);
    public static final Setting HIDE_CREATE_BUTTON = new Setting("revanced_hide_create_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_CROWDFUNDING_BOX = new Setting("revanced_hide_crowdfunding_box", BOOLEAN, FALSE, true);
    public static final Setting HIDE_EMAIL_ADDRESS = new Setting("revanced_hide_email_address", BOOLEAN, FALSE);
    public static final Setting HIDE_EMERGENCY_BOX = new Setting("revanced_hide_emergency_box", BOOLEAN, TRUE);
    public static final Setting HIDE_ENDSCREEN_CARDS = new Setting("revanced_hide_endscreen_cards", BOOLEAN, TRUE);
    public static final Setting HIDE_EXPANDABLE_CHIP = new Setting("revanced_hide_expandable_chip", BOOLEAN, TRUE);
    public static final Setting HIDE_FEED_SURVEY = new Setting("revanced_hide_feed_survey", BOOLEAN, TRUE);
    public static final Setting HIDE_FILTER_BAR_FEED_IN_FEED = new Setting("revanced_hide_filter_bar_feed_in_feed", BOOLEAN, FALSE, true);
    public static final Setting HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS = new Setting("revanced_hide_filter_bar_feed_in_related_videos", BOOLEAN, FALSE, true);
    public static final Setting HIDE_FILTER_BAR_FEED_IN_SEARCH = new Setting("revanced_hide_filter_bar_feed_in_search", BOOLEAN, FALSE, true);
    public static final Setting HIDE_FLOATING_MICROPHONE_BUTTON = new Setting("revanced_hide_floating_microphone_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_FULLSCREEN_PANELS = new Setting("revanced_hide_fullscreen_panels", BOOLEAN, TRUE, true);
    public static final Setting HIDE_GRAY_SEPARATOR = new Setting("revanced_hide_gray_separator", BOOLEAN, TRUE);
    public static final Setting HIDE_HIDE_CHANNEL_GUIDELINES = new Setting("revanced_hide_channel_guidelines", BOOLEAN, TRUE);
    public static final Setting HIDE_HIDE_INFO_PANELS = new Setting("revanced_hide_info_panels", BOOLEAN, TRUE);
    public static final Setting HIDE_HOME_BUTTON = new Setting("revanced_hide_home_button", BOOLEAN, FALSE, true);
    public static final Setting HIDE_IMAGE_SHELF = new Setting("revanced_hide_image_shelf", BOOLEAN, TRUE);
    public static final Setting HIDE_INFO_CARDS = new Setting("revanced_hide_info_cards", BOOLEAN, TRUE);
    public static final Setting HIDE_JOIN_MEMBERSHIP_BUTTON = new Setting("revanced_hide_join_membership_button", BOOLEAN, TRUE);
    public static final Setting HIDE_LOAD_MORE_BUTTON = new Setting("revanced_hide_load_more_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_MEDICAL_PANELS = new Setting("revanced_hide_medical_panels", BOOLEAN, TRUE);
    public static final Setting HIDE_MIX_PLAYLISTS = new Setting("revanced_hide_mix_playlists", BOOLEAN, TRUE);
    public static final Setting HIDE_MOVIES_SECTION = new Setting("revanced_hide_movies_section", BOOLEAN, TRUE);
    public static final Setting HIDE_NOTIFY_ME_BUTTON = new Setting("revanced_hide_notify_me_button", BOOLEAN, TRUE);
    public static final Setting HIDE_PLAYER_BUTTONS = new Setting("revanced_hide_player_buttons", BOOLEAN, FALSE);
    public static final Setting HIDE_PREVIEW_COMMENT = new Setting("revanced_hide_preview_comment", BOOLEAN, FALSE, true);
    public static final Setting HIDE_QUICK_ACTIONS = new Setting("revanced_hide_quick_actions", BOOLEAN, FALSE);
    public static final Setting HIDE_RELATED_VIDEOS = new Setting("revanced_hide_related_videos", BOOLEAN, FALSE);
    public static final Setting HIDE_SEARCH_RESULT_SHELF_HEADER = new Setting("revanced_hide_search_result_shelf_header", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_BUTTON = new Setting("revanced_hide_shorts_button", BOOLEAN, TRUE, true);
    public static final Setting HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES = new Setting("revanced_hide_subscribers_community_guidelines", BOOLEAN, TRUE);
    public static final Setting HIDE_SUBSCRIPTIONS_BUTTON = new Setting("revanced_hide_subscriptions_button", BOOLEAN, FALSE, true);
    public static final Setting HIDE_TIMED_REACTIONS = new Setting("revanced_hide_timed_reactions", BOOLEAN, TRUE);
    public static final Setting HIDE_TIMESTAMP = new Setting("revanced_hide_timestamp", BOOLEAN, FALSE);
    /** @noinspection DeprecatedIsStillUsed*/
    @Deprecated
    public static final Setting HIDE_VIDEO_WATERMARK = new Setting("revanced_hide_video_watermark", BOOLEAN, TRUE);
    public static final Setting HIDE_VIDEO_CHANNEL_WATERMARK = new Setting("revanced_hide_channel_watermark", BOOLEAN, TRUE);
    public static final Setting HIDE_FOR_YOU_SHELF = new Setting("revanced_hide_for_you_shelf", BOOLEAN, TRUE);
    public static final Setting HIDE_VIDEO_QUALITY_MENU_FOOTER = new Setting("revanced_hide_video_quality_menu_footer", BOOLEAN, TRUE);
    public static final Setting HIDE_SEARCH_RESULT_RECOMMENDATIONS = new Setting("revanced_hide_search_result_recommendations", BOOLEAN, TRUE);
    public static final Setting PLAYER_OVERLAY_OPACITY = new Setting("revanced_player_overlay_opacity", INTEGER, 100, true);
    public static final Setting PLAYER_POPUP_PANELS = new Setting("revanced_hide_player_popup_panels", BOOLEAN, FALSE);
    public static final Setting SPOOF_APP_VERSION = new Setting("revanced_spoof_app_version", BOOLEAN, FALSE, true, "revanced_spoof_app_version_user_dialog_message");
    public static final Setting SPOOF_APP_VERSION_TARGET = new Setting("revanced_spoof_app_version_target", STRING, "17.08.35", true, getParents(SPOOF_APP_VERSION));
    public static final Setting SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON = new Setting("revanced_switch_create_with_notifications_button", BOOLEAN, TRUE, true);
    public static final Setting TABLET_LAYOUT = new Setting("revanced_tablet_layout", BOOLEAN, FALSE, true, "revanced_tablet_layout_user_dialog_message");
    public static final Setting USE_TABLET_MINIPLAYER = new Setting("revanced_tablet_miniplayer", BOOLEAN, FALSE, true);
    public static final Setting WIDE_SEARCHBAR = new Setting("revanced_wide_searchbar", BOOLEAN, FALSE, true);
    public static final Setting START_PAGE = new Setting("revanced_start_page", STRING, "");

    // Description
    public static final Setting HIDE_CHAPTERS = new Setting("revanced_hide_chapters", BOOLEAN, TRUE);
    public static final Setting HIDE_INFO_CARDS_SECTION = new Setting("revanced_hide_info_cards_section", BOOLEAN, TRUE);
    public static final Setting HIDE_GAME_SECTION = new Setting("revanced_hide_game_section", BOOLEAN, TRUE);
    public static final Setting HIDE_MUSIC_SECTION = new Setting("revanced_hide_music_section", BOOLEAN, TRUE);
    public static final Setting HIDE_PODCAST_SECTION = new Setting("revanced_hide_podcast_section", BOOLEAN, TRUE);
    public static final Setting HIDE_TRANSCIPT_SECTION = new Setting("revanced_hide_transcript_section", BOOLEAN, TRUE);

    // Shorts
    public static final Setting HIDE_SHORTS = new Setting("revanced_hide_shorts", BOOLEAN, FALSE, true);
    public static final Setting HIDE_SHORTS_JOIN_BUTTON = new Setting("revanced_hide_shorts_join_button", BOOLEAN, TRUE);
    public static final Setting HIDE_SHORTS_SUBSCRIBE_BUTTON = new Setting("revanced_hide_shorts_subscribe_button", BOOLEAN, TRUE);
    public static final Setting HIDE_SHORTS_SUBSCRIBE_BUTTON_PAUSED = new Setting("revanced_hide_shorts_subscribe_button_paused", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_THANKS_BUTTON = new Setting("revanced_hide_shorts_thanks_button", BOOLEAN, TRUE);
    public static final Setting HIDE_SHORTS_COMMENTS_BUTTON = new Setting("revanced_hide_shorts_comments_button", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_REMIX_BUTTON = new Setting("revanced_hide_shorts_remix_button", BOOLEAN, TRUE);
    public static final Setting HIDE_SHORTS_SHARE_BUTTON = new Setting("revanced_hide_shorts_share_button", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_INFO_PANEL = new Setting("revanced_hide_shorts_info_panel", BOOLEAN, TRUE);
    public static final Setting HIDE_SHORTS_SOUND_BUTTON = new Setting("revanced_hide_shorts_sound_button", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_CHANNEL_BAR = new Setting("revanced_hide_shorts_channel_bar", BOOLEAN, FALSE);
    public static final Setting HIDE_SHORTS_NAVIGATION_BAR = new Setting("revanced_hide_shorts_navigation_bar", BOOLEAN, TRUE, true);

    /** @noinspection DeprecatedIsStillUsed*/ // Seekbar
    @Deprecated
    public static final Setting ENABLE_OLD_SEEKBAR_THUMBNAILS = new Setting("revanced_enable_old_seekbar_thumbnails", BOOLEAN, TRUE);
    public static final Setting RESTORE_OLD_SEEKBAR_THUMBNAILS = new Setting("revanced_restore_old_seekbar_thumbnails", BOOLEAN, TRUE);
    public static final Setting HIDE_SEEKBAR = new Setting("revanced_hide_seekbar", BOOLEAN, FALSE);
    public static final Setting HIDE_SEEKBAR_THUMBNAIL = new Setting("revanced_hide_seekbar_thumbnail", BOOLEAN, FALSE);
    public static final Setting SEEKBAR_CUSTOM_COLOR = new Setting("revanced_seekbar_custom_color", BOOLEAN, TRUE, true);
    public static final Setting SEEKBAR_CUSTOM_COLOR_VALUE = new Setting("revanced_seekbar_custom_color_value", STRING, "#FF0000", true, getParents(SEEKBAR_CUSTOM_COLOR));

    // Action buttons
    public static final Setting HIDE_LIKE_DISLIKE_BUTTON = new Setting("revanced_hide_like_dislike_button", BOOLEAN, FALSE);
    public static final Setting HIDE_LIVE_CHAT_BUTTON = new Setting("revanced_hide_live_chat_button", BOOLEAN, FALSE);
    public static final Setting HIDE_SHARE_BUTTON = new Setting("revanced_hide_share_button", BOOLEAN, FALSE);
    public static final Setting HIDE_REPORT_BUTTON = new Setting("revanced_hide_report_button", BOOLEAN, FALSE);
    public static final Setting HIDE_REMIX_BUTTON = new Setting("revanced_hide_remix_button", BOOLEAN, TRUE);
    public static final Setting HIDE_DOWNLOAD_BUTTON = new Setting("revanced_hide_download_button", BOOLEAN, FALSE);
    public static final Setting HIDE_THANKS_BUTTON = new Setting("revanced_hide_thanks_button", BOOLEAN, TRUE);
    public static final Setting HIDE_CLIP_BUTTON = new Setting("revanced_hide_clip_button", BOOLEAN, TRUE);
    public static final Setting HIDE_PLAYLIST_BUTTON = new Setting("revanced_hide_playlist_button", BOOLEAN, FALSE);
    public static final Setting HIDE_SHOP_BUTTON = new Setting("revanced_hide_shop_button", BOOLEAN, TRUE);

    // Player flyout menu items
    public static final Setting HIDE_CAPTIONS_MENU = new Setting("revanced_hide_player_flyout_captions", BOOLEAN, FALSE);
    public static final Setting HIDE_ADDITIONAL_SETTINGS_MENU = new Setting("revanced_hide_player_flyout_additional_settings", BOOLEAN, FALSE);
    public static final Setting HIDE_LOOP_VIDEO_MENU = new Setting("revanced_hide_player_flyout_loop_video", BOOLEAN, FALSE);
    public static final Setting HIDE_AMBIENT_MODE_MENU = new Setting("revanced_hide_player_flyout_ambient_mode", BOOLEAN, FALSE);
    public static final Setting HIDE_REPORT_MENU = new Setting("revanced_hide_player_flyout_report", BOOLEAN, TRUE);
    public static final Setting HIDE_HELP_MENU = new Setting("revanced_hide_player_flyout_help", BOOLEAN, TRUE);
    public static final Setting HIDE_SPEED_MENU = new Setting("revanced_hide_player_flyout_speed", BOOLEAN, FALSE);
    public static final Setting HIDE_MORE_INFO_MENU = new Setting("revanced_hide_player_flyout_more_info", BOOLEAN, TRUE);
    public static final Setting HIDE_AUDIO_TRACK_MENU = new Setting("revanced_hide_player_flyout_audio_track", BOOLEAN, FALSE);
    public static final Setting HIDE_WATCH_IN_VR_MENU = new Setting("revanced_hide_player_flyout_watch_in_vr", BOOLEAN, TRUE);

    // Misc
    public static final Setting AUTO_CAPTIONS = new Setting("revanced_auto_captions", BOOLEAN, FALSE);
    public static final Setting DISABLE_ZOOM_HAPTICS = new Setting("revanced_disable_zoom_haptics", BOOLEAN, TRUE);
    public static final Setting EXTERNAL_BROWSER = new Setting("revanced_external_browser", BOOLEAN, TRUE, true);
    public static final Setting AUTO_REPEAT = new Setting("revanced_auto_repeat", BOOLEAN, FALSE);
    public static final Setting SEEKBAR_TAPPING = new Setting("revanced_seekbar_tapping", BOOLEAN, TRUE);
    public static final Setting SLIDE_TO_SEEK = new Setting("revanced_slide_to_seek", BOOLEAN, FALSE);
    /** @noinspection DeprecatedIsStillUsed*/
    @Deprecated
    public static final Setting DISABLE_FINE_SCRUBBING_GESTURE = new Setting("revanced_disable_fine_scrubbing_gesture", BOOLEAN, TRUE);
    public static final Setting DISABLE_PRECISE_SEEKING_GESTURE = new Setting("revanced_disable_precise_seeking_gesture", BOOLEAN, TRUE);
    public static final Setting SPOOF_SIGNATURE = new Setting("revanced_spoof_signature_verification_enabled", BOOLEAN, TRUE, true,
            "revanced_spoof_signature_verification_enabled_user_dialog_message");
    public static final Setting SPOOF_SIGNATURE_IN_FEED = new Setting("revanced_spoof_signature_in_feed_enabled", BOOLEAN, FALSE, false,
            getParents(SPOOF_SIGNATURE));
    public static final Setting SPOOF_STORYBOARD_RENDERER = new Setting("revanced_spoof_storyboard", BOOLEAN, TRUE, true,
            getParents(SPOOF_SIGNATURE));
    public static final Setting SPOOF_DEVICE_DIMENSIONS = new Setting("revanced_spoof_device_dimensions", BOOLEAN, FALSE, true);
    public static final Setting BYPASS_URL_REDIRECTS = new Setting("revanced_bypass_url_redirects", BOOLEAN, TRUE);
    public static final Setting ANNOUNCEMENTS = new Setting("revanced_announcements", BOOLEAN, TRUE);
    public static final Setting ANNOUNCEMENT_CONSUMER = new Setting("revanced_announcement_consumer", STRING, "");
    public static final Setting ANNOUNCEMENT_LAST_HASH = new Setting("revanced_announcement_last_hash", STRING, "");
    public static final Setting REMOVE_TRACKING_QUERY_PARAMETER = new Setting("revanced_remove_tracking_query_parameter", BOOLEAN, TRUE);
    public static final Setting REMOVE_VIEWER_DISCRETION_DIALOG= new Setting("revanced_remove_viewer_discretion_dialog", BOOLEAN, FALSE,
            "revanced_remove_viewer_discretion_dialog_user_dialog_message");

    // Swipe controls
    public static final Setting SWIPE_BRIGHTNESS = new Setting("revanced_swipe_brightness", BOOLEAN, TRUE);
    public static final Setting SWIPE_VOLUME = new Setting("revanced_swipe_volume", BOOLEAN, TRUE);
    public static final Setting SWIPE_PRESS_TO_ENGAGE = new Setting("revanced_swipe_press_to_engage", BOOLEAN, FALSE, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_HAPTIC_FEEDBACK = new Setting("revanced_swipe_haptic_feedback", BOOLEAN, TRUE, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_MAGNITUDE_THRESHOLD = new Setting("revanced_swipe_threshold", INTEGER, 30, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_OVERLAY_BACKGROUND_ALPHA = new Setting("revanced_swipe_overlay_background_alpha", INTEGER, 127, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_OVERLAY_TEXT_SIZE = new Setting("revanced_swipe_text_overlay_size", INTEGER, 22, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_OVERLAY_TIMEOUT = new Setting("revanced_swipe_overlay_timeout", LONG, 500L, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final Setting SWIPE_SAVE_AND_RESTORE_BRIGHTNESS = new Setting("revanced_swipe_save_and_restore_brightness", BOOLEAN, TRUE, true,
            getParents(SWIPE_BRIGHTNESS, SWIPE_VOLUME));

    // Debugging
    public static final Setting DEBUG_PROTOBUFFER = new Setting("revanced_debug_protobuffer", BOOLEAN, FALSE, getParents(app.revanced.integrations.shared.settings.Settings.DEBUG));

    // ReturnYoutubeDislike
    public static final Setting RYD_ENABLED = new Setting("ryd_enabled", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE);
    public static final Setting RYD_USER_ID = new Setting("ryd_user_id", STRING, "", RETURN_YOUTUBE_DISLIKE);
    public static final Setting RYD_SHORTS = new Setting("ryd_shorts", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE, getParents(RYD_ENABLED));
    public static final Setting RYD_DISLIKE_PERCENTAGE = new Setting("ryd_dislike_percentage", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, getParents(RYD_ENABLED));
    public static final Setting RYD_COMPACT_LAYOUT = new Setting("ryd_compact_layout", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, getParents(RYD_ENABLED));
    public static final Setting RYD_TOAST_ON_CONNECTION_ERROR = new Setting("ryd_toast_on_connection_error", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE, getParents(RYD_ENABLED));

    // SponsorBlock
    public static final Setting SB_ENABLED = new Setting("sb_enabled", BOOLEAN, TRUE, SPONSOR_BLOCK);
    public static final Setting SB_PRIVATE_USER_ID = new Setting("sb_private_user_id_Do_Not_Share", STRING, "", SPONSOR_BLOCK);

    /**
     * Do not use directly, instead use {@link SponsorBlockSettings}
     */
    public static final Setting DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING = new Setting("uuid", STRING, "", SPONSOR_BLOCK); // Delete sometime in 2024
    public static final Setting SB_CREATE_NEW_SEGMENT_STEP = new Setting("sb_create_new_segment_step", INTEGER, 150, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_VOTING_BUTTON = new Setting("sb_voting_button", BOOLEAN, FALSE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_CREATE_NEW_SEGMENT = new Setting("sb_create_new_segment", BOOLEAN, FALSE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_COMPACT_SKIP_BUTTON = new Setting("sb_compact_skip_button", BOOLEAN, FALSE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_AUTO_HIDE_SKIP_BUTTON = new Setting("sb_auto_hide_skip_button", BOOLEAN, TRUE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_TOAST_ON_SKIP = new Setting("sb_toast_on_skip", BOOLEAN, TRUE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_TOAST_ON_CONNECTION_ERROR = new Setting("sb_toast_on_connection_error", BOOLEAN, TRUE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_TRACK_SKIP_COUNT = new Setting("sb_track_skip_count", BOOLEAN, TRUE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_SEGMENT_MIN_DURATION = new Setting("sb_min_segment_duration", FLOAT, 0F, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_VIDEO_LENGTH_WITHOUT_SEGMENTS = new Setting("sb_video_length_without_segments", BOOLEAN, TRUE, SPONSOR_BLOCK, getParents(SB_ENABLED));
    public static final Setting SB_API_URL = new Setting("sb_api_url", STRING, "https://sponsor.ajay.app", SPONSOR_BLOCK);
    public static final Setting SB_USER_IS_VIP = new Setting("sb_user_is_vip", BOOLEAN, FALSE, SPONSOR_BLOCK);

    // SB Setting not exported
    public static final Setting SB_LAST_VIP_CHECK = new Setting("sb_last_vip_check", LONG, 0L, SPONSOR_BLOCK);
    public static final Setting SB_HIDE_EXPORT_WARNING = new Setting("sb_hide_export_warning", BOOLEAN, FALSE, SPONSOR_BLOCK);
    public static final Setting SB_SEEN_GUIDELINES = new Setting("sb_seen_guidelines", BOOLEAN, FALSE, SPONSOR_BLOCK);
    public static final Setting SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS = new Setting("sb_local_time_saved_number_segments", INTEGER, 0, SPONSOR_BLOCK);
    public static final Setting SB_LOCAL_TIME_SAVED_MILLISECONDS = new Setting("sb_local_time_saved_milliseconds", LONG, 0L, SPONSOR_BLOCK);

    static {
        // region Migration

        migrateOldSettingToNew(HIDE_VIDEO_WATERMARK, HIDE_VIDEO_CHANNEL_WATERMARK);
        migrateOldSettingToNew(DISABLE_FINE_SCRUBBING_GESTURE, DISABLE_PRECISE_SEEKING_GESTURE);
        migrateOldSettingToNew(SHOW_OLD_VIDEO_QUALITY_MENU, RESTORE_OLD_VIDEO_QUALITY_MENU);
        migrateOldSettingToNew(ENABLE_OLD_SEEKBAR_THUMBNAILS, RESTORE_OLD_SEEKBAR_THUMBNAILS);

        // Do _not_ delete this SB private user id migration property until sometime in 2024.
        // This is the only setting that cannot be reconfigured if lost,
        // and more time should be given for users who rarely upgrade.
        migrateOldSettingToNew(DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING, SB_PRIVATE_USER_ID);

        // This migration may need to remain here for a while.
        // Older online guides will still reference using commas,
        // and this code will automatically convert anything the user enters to newline format,
        // and also migrate any imported older settings that using commas.
        String componentsToFilter = Settings.CUSTOM_FILTER_STRINGS.getString();
        if (componentsToFilter.contains(",")) {
            Logger.printInfo(() -> "Migrating custom filter strings to new line format");
            Settings.CUSTOM_FILTER_STRINGS.saveValue(componentsToFilter.replace(",", "\n"));
        }

        // endregion
    }
}
