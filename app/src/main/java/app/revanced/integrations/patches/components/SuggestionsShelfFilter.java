package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.NavBarIndexHook;

public class SuggestionsShelfFilter extends Filter {

    public SuggestionsShelfFilter() {
        pathFilterGroups.addAll(
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
        if (NavBarIndexHook.isHomeTab())
            // When the Home Tab is detected, hide the suggestion shelf
            return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

        return false;
    }
}
