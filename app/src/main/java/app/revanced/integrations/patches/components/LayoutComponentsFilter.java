package app.revanced.integrations.patches.components;


import android.os.Build;
import android.view.View;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;


public final class LayoutComponentsFilter extends Filter {
    private final String[] exceptions;

    private final CustomFilterGroup custom;

    // region Mix playlists
    private final ByteArrayAsStringFilterGroup mixPlaylists;
    private final ByteArrayAsStringFilterGroup imageHosting;

    // endregion

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LayoutComponentsFilter() {
        exceptions = new String[]{
                "home_video_with_context",
                "related_video_with_context",
                "comment_thread", // skip filtering anything in the comments
                "|comment.", // skip filtering anything in the comments replies
                "library_recent_shelf",
        };

        custom = new CustomFilterGroup(
                SettingsEnum.CUSTOM_FILTER,
                SettingsEnum.CUSTOM_FILTER_STRINGS
        );

        final var communityPosts = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper"
        );

        final var communityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_GUIDELINES,
                "community_guidelines"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES,
                "sponsorships_comments_upsell"
        );


        final var channelMemberShelf = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_MEMBER_SHELF,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                SettingsEnum.HIDE_COMPACT_BANNER,
                "compact_banner"
        );

        final var inFeedSurvey = new StringFilterGroup(
                SettingsEnum.HIDE_FEED_SURVEY,
                "in_feed_survey",
                "slimline_survey"
        );

        final var medicalPanel = new StringFilterGroup(
                SettingsEnum.HIDE_MEDICAL_PANELS,
                "medical_panel"
        );

        final var paidContent = new StringFilterGroup(
                SettingsEnum.HIDE_PAID_CONTENT,
                "paid_content_overlay"
        );

        final var infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_INFO_PANELS,
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_CHANNEL_GUIDELINES,
                "channel_guidelines_entry_banner"
        );

        final var audioTrackButton = new StringFilterGroup(
                SettingsEnum.HIDE_AUDIO_TRACK_BUTTON,
                "multi_feed_icon_button"
        );

        final var artistCard = new StringFilterGroup(
                SettingsEnum.HIDE_ARTIST_CARDS,
                "official_card"
        );

        final var expandableMetadata = new StringFilterGroup(
                SettingsEnum.HIDE_EXPANDABLE_CHIP,
                "inline_expander"
        );

        final var chapters = new StringFilterGroup(
                SettingsEnum.HIDE_CHAPTERS,
                "macro_markers_carousel"
        );

        final var channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                SettingsEnum.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var quickActions = new StringFilterGroup(
                SettingsEnum.HIDE_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                SettingsEnum.HIDE_IMAGE_SHELF,
                "image_shelf"
        );

        final var graySeparator = new StringFilterGroup(
                SettingsEnum.HIDE_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );

        // region Mix playlists

        mixPlaylists = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_MIX_PLAYLISTS,
                "&list=",
                "YouTube Music"
        );

        imageHosting = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_MIX_PLAYLISTS, // Unused
                "ggpht.com"
        );

        // endregion

        this.pathFilterGroups.addAll(
                channelBar,
                communityPosts,
                paidContent,
                latestPosts,
                chapters,
                communityGuidelines,
                quickActions,
                expandableMetadata,
                relatedVideos,
                compactBanner,
                inFeedSurvey,
                medicalPanel,
                infoPanel,
                channelGuidelines,
                audioTrackButton,
                artistCard,
                imageShelf,
                subscribersCommunityGuidelines,
                channelMemberShelf
        );

        final var carouselAd = new StringFilterGroup(
                SettingsEnum.HIDE_GENERAL_ADS,
                "carousel_ad"
        );

        this.identifierFilterGroups.addAll(
                graySeparator,
                carouselAd
        );
    }

    private boolean isMixPlaylistFiltered(final byte[] _protobufBufferArray) {
        if (!mixPlaylists.isEnabled()) return false;

        // Two checks are required to prevent false positives.

        // First check if the current buffer potentially contains a mix playlist.
        if (!mixPlaylists.check(_protobufBufferArray).isFiltered()) return false;

        // Ensure that the buffer actually contains a mix playlist.
        return imageHosting.check(_protobufBufferArray).isFiltered();
    }

    @Override
    public boolean isFiltered(final String path, final String identifier, final byte[] _protobufBufferArray) {
        if (custom.isEnabled() && custom.check(path).isFiltered())
            return true;

        if (ReVancedUtils.containsAny(path, exceptions))
            return false; // Exceptions are not filtered.

        if (super.isFiltered(path, identifier, _protobufBufferArray))
            return true;

        return isMixPlaylistFiltered(_protobufBufferArray);
    }

    /**
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        ReVancedUtils.hideViewBy1dpUnderCondition(SettingsEnum.HIDE_GENERAL_ADS, view);
    }
}
