package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class SuggestionsShelvesFilter extends Filter {

    public SuggestionsShelvesFilter() {
        addPathCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_SUGGESTIONS_SHELVES,
                        "horizontal_video_shelf.eml",
                        "horizontal_shelf.eml"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex == 0 && shouldHideShelves()) {
            return super.isFiltered(path, identifier, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        return false;
    }

    private static boolean shouldHideShelves() {
        // Only filter if the library tab is not selected.
        // This check is important as the suggestion shelf layout is used for the library tab playlists.
        return !NavigationBar.NavigationButton.libraryOrYouTabIsSelected()
                // But if the player is opened while library is selected,
                // then still filter any recommendations below the player.
                || PlayerType.getCurrent().isMaximizedOrFullscreen()
                // Or if the search is active while library is selected, then also filter.
                || NavigationBar.isSearchBarActive();
    }
}
