package app.revanced.integrations.patches.components;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.NavBarIndexHook;
import app.revanced.integrations.utils.ReVancedUtils;

public class SuggestionsShelfFilter extends Filter {

    /**
     * When spoofing to app version 17.08.35 or older, the watch history preview bar uses
     * the same layout components as the breaking news shelf.
     */
    private static final boolean isSpoofingOldVersionWithHorizontalCardListWatchHistory =
            SettingsEnum.SPOOF_APP_VERSION.getBoolean()
                    && SettingsEnum.SPOOF_APP_VERSION_TARGET.getString().compareTo("17.08.35") <= 0;

    // Must be volatile or synchronized, as litho filtering runs off main thread and this field is then access from the main thread.
    public static volatile boolean isLibraryRecentShelfVisible;
    public static volatile boolean isHomeFeedVisible;

    public SuggestionsShelfFilter() {
        pathFilterGroupList.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_SUGGESTIONS_SHELF,
                        "horizontal_video_shelf.eml"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        // Only one filter is added, so the matched group must be the horizontal video shelf.
        if (path.contains("library_recent_shelf")) {
            // If the library shelf is detected, set the current navbar index to 4
            NavBarIndexHook.setCurrentNavBarIndex(4);
            isLibraryRecentShelfVisible = true;
        } else if (path.contains("more_drawer")) {
            // If drawer button is detected, set the current NavBar index to zero
            NavBarIndexHook.setCurrentNavBarIndex(0);
            isHomeFeedVisible = true;
        } else if (NavBarIndexHook.isNotLibraryTab())
            // When the Library Tab is not detected, hide the suggestion shelf
            return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

        return false;
    }

    /**
     * Injection point.
     * <p>
     * Only used to hide breaking news on tablet layout which still uses the old UI components.
     */
    public static void hideBreakingNews(View view) {
        if (!SettingsEnum.HIDE_SUGGESTIONS_SHELF.getBoolean()
                // If this starts hiding stuff inappropriately on phones,
                // then can add a check if the device is not a tablet using ReVancedUtils.isTablet()
                || isSpoofingOldVersionWithHorizontalCardListWatchHistory) return;

        ReVancedUtils.hideViewByLayoutParams(view);
    }
}
