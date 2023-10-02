package app.revanced.integrations.patches.components;

import android.os.Build;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.settings.SettingsEnum;
import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import static app.revanced.integrations.utils.ReVancedUtils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.utils.ReVancedUtils.hideViewUnderCondition;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ShortsFilter extends Filter {
    public static PivotBar pivotBar; // Set by patch.
    private final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";

    private final StringFilterGroup channelBar;
    private final StringFilterGroup soundButton;
    private final StringFilterGroup infoPanel;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup videoActionButton;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();

    public ShortsFilter() {
        var shorts = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"

        );
        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shelf_header.eml"
        );

        // Home / subscription feed components.
        var thanksButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_THANKS_BUTTON,
                "suggested_action"
        );

        identifierFilterGroupList.addAll(shorts, shelfHeader, thanksButton);

        // Shorts player components.
        var joinButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );
        var subscribeButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button",
                "shorts_paused_state"
        );

        channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_CHANNEL_BAR,
                REEL_CHANNEL_BAR_PATH
        );
        soundButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        videoActionButton = new StringFilterGroup(
                null,
                "ContainerType|shorts_video_action_button"
        );

        pathFilterGroupList.addAll(
                joinButton, subscribeButton, channelBar, soundButton, infoPanel, videoActionButton
        );

        var shortsCommentButton = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_SHORTS_COMMENTS_BUTTON,
                "reel_comment_button"
        );

        var shortsShareButton = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SHARE_BUTTON,
                "reel_share_button"
        );

        var shortsRemixButton = new ByteArrayAsStringFilterGroup(
                SettingsEnum.HIDE_SHORTS_REMIX_BUTTON,
                "reel_remix_button"
        );

        videoActionButtonGroupList.addAll(shortsCommentButton, shortsShareButton, shortsRemixButton);
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedList == pathFilterGroupList) {
            // Always filter if matched.
            if (matchedGroup == soundButton || matchedGroup == infoPanel || matchedGroup == channelBar)
                return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

            // Video action buttons (comment, share, remix) have the same path.
            if (matchedGroup == videoActionButton) {
                if (videoActionButtonGroupList.check(protobufBufferArray).isFiltered())
                    return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
                return false;
            }

            // Filter other path groups from pathFilterGroupList, only when reelChannelBar is visible
            // to avoid false positives.
            if (!path.startsWith(REEL_CHANNEL_BAR_PATH))
                return false;
        } else if (matchedGroup == shelfHeader) {
            // Because the header is used in watch history and possibly other places, check for the index,
            // which is 0 when the shelf header is used for Shorts.
            if (matchedIndex != 0) return false;
        }

        // Super class handles logging.
        return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }

    public static void hideShortsShelf(final View shortsShelfView) {
        hideViewBy1dpUnderCondition(SettingsEnum.HIDE_SHORTS, shortsShelfView);
    }

    // region Hide the buttons in older versions of YouTube. New versions use Litho.

    public static void hideShortsCommentsButton(final View commentsButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_COMMENTS_BUTTON, commentsButtonView);
    }

    public static void hideShortsRemixButton(final View remixButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_REMIX_BUTTON, remixButtonView);
    }

    public static void hideShortsShareButton(final View shareButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_SHARE_BUTTON, shareButtonView);
    }

    // endregion

    public static void hideNavigationBar() {
        if (!SettingsEnum.HIDE_SHORTS_NAVIGATION_BAR.getBoolean()) return;
        if (pivotBar == null) return;

        pivotBar.setVisibility(View.GONE);
    }

    public static View hideNavigationBar(final View navigationBarView) {
        if (SettingsEnum.HIDE_SHORTS_NAVIGATION_BAR.getBoolean())
            return null; // Hides the navigation bar.

        return navigationBarView;
    }
}
