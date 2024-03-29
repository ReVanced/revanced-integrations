package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;

@SuppressWarnings("unused")
public final class SuggestionsShelfFilter extends Filter {

    public SuggestionsShelfFilter() {
        addPathCallbacks(
                new StringFilterGroup(
                        null, // Setting is based on navigation state.
                        "horizontal_video_shelf.eml",
                        "horizontal_shelf.eml"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray, StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (shouldHideSuggestionsShelf() && contentIndex == 0)
            return super.isFiltered(path, identifier, protobufBufferArray, matchedGroup, contentType, contentIndex);

        return false;
    }

    private static boolean shouldHideSuggestionsShelf() {
        return Settings.HIDE_SUGGESTIONS_SHELF.get() &&
                NavigationBar.NavigationButton.HOME.isSelected();
    }
}
