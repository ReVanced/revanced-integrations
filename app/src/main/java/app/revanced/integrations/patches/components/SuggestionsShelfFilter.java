package app.revanced.integrations.patches.components;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.NavBarIndexHook;
import app.revanced.integrations.utils.ReVancedUtils;

public class SuggestionsShelfFilter extends Filter {

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
        if (path.contains("library_recent_shelf"))
            // If the library shelf is detected, set the current navbar index to 4
            NavBarIndexHook.setCurrentNavBarIndex(4);

        else if (NavBarIndexHook.isNotLibraryTab())
            // When the Library Tab is not detected, hide the suggestion shelf
            return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

        return false;
    }

    /**
     * Injection point.
     */
    public static void hideBreakingNews(View view) {
        if (!SettingsEnum.HIDE_SUGGESTIONS_SHELF.getBoolean()
                || SettingsEnum.SPOOF_APP_VERSION.getBoolean()) return;

        ReVancedUtils.hideViewByLayoutParams(view);
    }
}
