package app.revanced.integrations.youtube.patches.components;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;

@SuppressWarnings("unused")
public final class SuggestionsShelfFilter extends Filter {

    /**
     * When spoofing to app versions less than 18.01.00 and older, the watch history preview bar uses
     * the same layout components as the breaking news shelf.
     *
     * Breaking news does not appear to be present in these older versions anyways.
     */
    private static final boolean isSpoofingOldVersionWithHorizontalCardListWatchHistory =
            SpoofAppVersionPatch.isSpoofingToLessThan("18.01.00");

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
        if (NavigationBar.NavigationButton.HOME.isSelected())
            return Settings.HIDE_SUGGESTIONS_SHELF.get();

        return false;
    }

    /**
     * Injection point.
     */
    public static void hideBreakingNews(View view) {
        if (!Settings.HIDE_SUGGESTIONS_SHELF.get()
            || isSpoofingOldVersionWithHorizontalCardListWatchHistory) return;

        Utils.hideViewByLayoutParams(view);
    }
}
