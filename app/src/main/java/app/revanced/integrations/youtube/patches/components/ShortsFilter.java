package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.shared.Utils.hideViewUnderCondition;
import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class ShortsFilter extends Filter {
    public static PivotBar pivotBar; // Set by patch.

    private final static String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";
    /**
     * For paid promotion label and subscribe button that appears in the channel bar.
     */
    private final static String REEL_METAPANEL_PATH = "reel_metapanel.eml";

    private final StringFilterGroup shortsCompactFeedVideoPath;
    private final ByteArrayFilterGroup shortsCompactFeedVideoBuffer;

    private final StringFilterGroup subscribeButton;
    private final StringFilterGroup joinButton;
    private final StringFilterGroup paidPromotionButton;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup suggestedAction;
    private final ByteArrayFilterGroupList suggestedActionsGroupList =  new ByteArrayFilterGroupList();

    private final StringFilterGroup actionBar;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();

    public ShortsFilter() {
        //
        // Identifier components.
        //

        var shortsIdentifiers = new StringFilterGroup(
                null, // Setting is based on navigation state.
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"
        );

        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                null,
                "shelf_header.eml"
        );

        addIdentifierCallbacks(shortsIdentifiers, shelfHeader);

        //
        // Path components.
        //

        shortsCompactFeedVideoPath = new StringFilterGroup(null,
                // Shorts that appear in the feed/search when the device is using tablet layout.
                "compact_video.eml",
                // Search results that appear in a horizontal shelf.
                "video_card.eml");

        // Filter out items that use the 'frame0' thumbnail.
        // This is a valid thumbnail for both regular videos and Shorts,
        // but it appears these thumbnails are used only for Shorts.
        shortsCompactFeedVideoBuffer = new ByteArrayFilterGroup(null, "/frame0.jpg");

        // Shorts player components.
        StringFilterGroup pausedOverlayButtons = new StringFilterGroup(
                Settings.HIDE_SHORTS_PAUSED_OVERLAY_BUTTONS,
                "shorts_paused_state"
        );

        StringFilterGroup channelBar = new StringFilterGroup(
                Settings.HIDE_SHORTS_CHANNEL_BAR,
                REEL_CHANNEL_BAR_PATH
        );

        StringFilterGroup fullVideoLinkLabel = new StringFilterGroup(
                Settings.HIDE_SHORTS_FULL_VIDEO_LINK_LABEL,
                "reel_multi_format_link"
        );

        StringFilterGroup videoTitle = new StringFilterGroup(
                Settings.HIDE_SHORTS_VIDEO_TITLE,
                "shorts_video_title_item"
        );

        StringFilterGroup reelSoundMetadata = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_METADATA_LABEL,
                "reel_sound_metadata"
        );

        StringFilterGroup soundButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        StringFilterGroup infoPanel = new StringFilterGroup(
                Settings.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        joinButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );

        subscribeButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );

        paidPromotionButton = new StringFilterGroup(
                Settings.HIDE_PAID_PROMOTION_LABEL,
                "reel_player_disclosure.eml"
        );

        actionBar = new StringFilterGroup(
                null,
                "shorts_action_bar"
        );

        suggestedAction = new StringFilterGroup(
                null,
                "suggested_action.eml"
        );

        addPathCallbacks(
                shortsCompactFeedVideoPath, suggestedAction, actionBar, joinButton, subscribeButton,
                paidPromotionButton, pausedOverlayButtons, channelBar, fullVideoLinkLabel, videoTitle,
                reelSoundMetadata, soundButton, infoPanel
        );

        //
        // Action buttons
        //
        videoActionButtonGroupList.addAll(
                // This also appears as the path item 'shorts_like_button.eml'
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_LIKE_BUTTON,
                        "reel_like_button",
                        "reel_like_toggled_button"
                ),
                // This also appears as the path item 'shorts_dislike_button.eml'
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_DISLIKE_BUTTON,
                        "reel_dislike_button",
                        "reel_dislike_toggled_button"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_COMMENTS_BUTTON,
                        "reel_comment_button"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SHARE_BUTTON,
                        "reel_share_button"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_REMIX_BUTTON,
                        "reel_remix_button"
                )
        );

        //
        // Suggested actions.
        //
        suggestedActionsGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SHOP_BUTTON,
                        "yt_outline_bag_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_TAGGED_PRODUCTS,
                        // Product buttons show pictures of the products, and does not have any unique icons to identify.
                        // Instead use a unique identifier found in the buffer.
                        "PAproduct_listZ"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_LOCATION_LABEL,
                        "yt_outline_location_point_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SAVE_SOUND_BUTTON,
                        "yt_outline_list_add_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SEARCH_SUGGESTIONS,
                        "yt_outline_search_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SUPER_THANKS_BUTTON,
                        "yt_outline_dollar_sign_heart_"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentType == FilterContentType.PATH) {
            if (matchedGroup == subscribeButton || matchedGroup == joinButton || matchedGroup == paidPromotionButton) {
                // Selectively filter to avoid false positive filtering of other subscribe/join buttons.
                if (path.startsWith(REEL_CHANNEL_BAR_PATH) || path.startsWith(REEL_METAPANEL_PATH)) {
                    return super.isFiltered(
                            identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                    );
                }
                return false;
            }

            if (matchedGroup == shortsCompactFeedVideoPath) {
                if (shouldHideShortsFeedItems() && shortsCompactFeedVideoBuffer.check(protobufBufferArray).isFiltered()) {
                    return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
                }
                return false;
            }

            // Video action buttons (like, dislike, comment, share, remix) have the same path.
            if (matchedGroup == actionBar) {
                if (videoActionButtonGroupList.check(protobufBufferArray).isFiltered()) {
                    return super.isFiltered(
                            identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                    );
                }
                return false;
            }

            if (matchedGroup == suggestedAction) {
                // Suggested actions can be at the start or in the middle of a path.
                if (suggestedActionsGroupList.check(protobufBufferArray).isFiltered()) {
                    return super.isFiltered(
                            identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                    );
                }
                return false;
            }

        } else {
            // Feed/search identifier components.
            if (matchedGroup == shelfHeader) {
                // Because the header is used in watch history and possibly other places, check for the index,
                // which is 0 when the shelf header is used for Shorts.
                if (contentIndex != 0) return false;
            }

            if (!shouldHideShortsFeedItems()) return false;
        }

        // Super class handles logging.
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    private static boolean shouldHideShortsFeedItems() {
        final boolean hideHome = Settings.HIDE_SHORTS_HOME.get();
        final boolean hideSubscriptions = Settings.HIDE_SHORTS_SUBSCRIPTIONS.get();
        final boolean hideSearch = Settings.HIDE_SHORTS_SEARCH.get();

        if (hideHome && hideSubscriptions && hideSearch) {
            // Shorts suggestions can load in the background if a video is opened and
            // then immediately minimized before any suggestions are loaded.
            // In this state the player type will show minimized, which makes it not possible to
            // distinguish between Shorts suggestions loading in the player and between
            // scrolling thru search/home/subscription tabs while a player is minimized.
            //
            // To avoid this situation for users that never want to show Shorts (all hide Shorts options are enabled)
            // then hide all Shorts everywhere including the Library history and Library playlists.
            return true;
        }

        // Must check player type first, as search bar can be active behind the player.
        if (PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            // For now, consider the under video results the same as the home feed.
            return hideHome;
        }

        // Must check second, as search can be from any tab.
        if (NavigationBar.isSearchBarActive()) {
            return hideSearch;
        }

        // Avoid checking navigation button status if all other Shorts should show.
        if (!hideHome && !hideSubscriptions) {
            return false;
        }

        NavigationButton selectedNavButton = NavigationButton.getSelectedNavigationButton();
        if (selectedNavButton == null) {
            return hideHome; // Unknown tab, treat the same as home.
        }
        if (selectedNavButton == NavigationButton.HOME) {
            return hideHome;
        }
        if (selectedNavButton == NavigationButton.SUBSCRIPTIONS) {
            return hideSubscriptions;
        }
        // User must be in the library tab.  Don't hide the history or any playlists here.
        return false;
    }

    public static void hideShortsShelf(final View shortsShelfView) {
        if (shouldHideShortsFeedItems()) {
            Utils.hideViewByLayoutParams(shortsShelfView);
        }
    }

    // region Hide the buttons in older versions of YouTube. New versions use Litho.

    public static void hideLikeButton(final View likeButtonView) {
        // Cannot set the visibility to gone for like/dislike,
        // as some other unknown YT code also sets the visibility after this hook.
        //
        // Setting the view to 0dp works, but that leaves a blank space where
        // the button was (only relevant for dislikes button).
        //
        // Instead remove the view from the parent.
        Utils.hideViewByRemovingFromParentUnderCondition(Settings.HIDE_SHORTS_LIKE_BUTTON, likeButtonView);
    }

    public static void hideDislikeButton(final View dislikeButtonView) {
        Utils.hideViewByRemovingFromParentUnderCondition(Settings.HIDE_SHORTS_DISLIKE_BUTTON, dislikeButtonView);
    }

    public static void hideShortsCommentsButton(final View commentsButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_COMMENTS_BUTTON, commentsButtonView);
    }

    public static void hideShortsRemixButton(final View remixButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_REMIX_BUTTON, remixButtonView);
    }

    public static void hideShortsShareButton(final View shareButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_SHARE_BUTTON, shareButtonView);
    }

    // endregion

    public static void hideNavigationBar() {
        if (!Settings.HIDE_SHORTS_NAVIGATION_BAR.get()) return;
        if (pivotBar == null) return;

        pivotBar.setVisibility(View.GONE);
    }

    public static View hideNavigationBar(final View navigationBarView) {
        if (Settings.HIDE_SHORTS_NAVIGATION_BAR.get())
            return null; // Hides the navigation bar.

        return navigationBarView;
    }
}
