package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.shared.NavigationBar;

/**
 * Used to discern a channel page is open in the home and subscription tabs.
 * All other navigation states can be used to rule out a channel page is open or not.
 */
@SuppressWarnings("unused")
public final class AlternativeThumbnailsFilter extends Filter {

    /**
     * If the last litho components loaded were part of a channel page.
     */
    private static volatile boolean channelPageWasLastLoaded;

    /**
     * Used to detect edge cases for viewing a channel page while changing tabs.
     */
    private static volatile boolean homeFeedTabIsSelected;

    public static boolean channelPageWasLastLoaded() {
        checkIfTabChanged();
        return channelPageWasLastLoaded;
    }

    /**
     * When switching tabs, the home and subscription feed litho may not be reloaded
     * but the image urls always are. This is particularly noticeable when using tablet mode.
     * Channel pages always seems to reload even when switching to
     * a tab that already is on a channel page.
     * Only need to check for changes of the home or subscription tab.
     *
     * Must check for tab change from both the litho and url hook.
     */
    private static void checkIfTabChanged() {
        final boolean homeSelected = NavigationBar.NavigationButton.HOME.isSelected();
        if (homeFeedTabIsSelected != homeSelected) {
            homeFeedTabIsSelected = homeSelected;
            channelPageWasLastLoaded = false;
        }
    }

    /**
     * Video components observed when viewing a channel page.
     */
    private final StringFilterGroup channelPageGroup = new StringFilterGroup(
            null,
            "channel_banner.eml",
            // Appears to be only used for the channel page.
            "mixed_content_shelf.eml",
            "grid_video_wrapper.eml",
            // Tablet layout components.
            "rich_grid_row.eml",
            // Appears in search and other places, but not in the home or subscription feed.
            "compact_video.eml"
    );

    /**
     * Video components never observed when viewing a channel page.
     *
     * Only include components found in the home and subscription tabs,
     * when not viewing a channel page.
     */
    private final StringFilterGroup notChannelPageGroup = new StringFilterGroup(
            null,
            // Home tab
            "home_video_with_context.eml",
            "library_recent_shelf.eml",
            "horizontal_tile_shelf.eml", // Continue watching and other home recommendations.
            // Subscription tab
            "subscriptions_channel_bar.eml",
            "video_metadata_carousel.eml",
            "video_with_context.eml"
    );

    public AlternativeThumbnailsFilter() {
        addPathCallbacks(channelPageGroup, notChannelPageGroup);
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex != 0) {
            return false; // All filters paths are at the start.
        }

        checkIfTabChanged();
        final boolean isChannelPageComponent = (matchedGroup == channelPageGroup);

        if (BaseSettings.DEBUG.get()) {
            if (channelPageWasLastLoaded != isChannelPageComponent) {
                Logger.printDebug(() -> "Channel page components: " + isChannelPageComponent);
            }
        }

        channelPageWasLastLoaded = isChannelPageComponent;

        return false;
    }
}