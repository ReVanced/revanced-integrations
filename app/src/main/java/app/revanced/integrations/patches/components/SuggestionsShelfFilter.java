package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.NavBarIndexHook;
import app.revanced.tiktok.settings.SettingsEnum;

public class SuggestionsShelfFilter extends Filter {
    private final StringFilterGroup horizontalVideoShelf;

    public SuggestionsShelfFilter() {
        pathFilterGroups.addAll(
                horizontalVideoShelf = new StringFilterGroup(
                        SettingsEnum.HIDE_SUGGESTIONS_SHELF,
                        "horizontal_video_shelf.eml"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray, FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedGroup == horizontalVideoShelf) {
            if (path.contains("library_recent_shelf"))
                // If the library shelf is detected, set the current navbar index to 4
                NavBarIndexHook.setCurrentNavBarIndex(4);
            if (NavBarIndexHook.isHomeTab())
                // When the Home Tab is detected, hide the suggestion shelf
                return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
        }

        return false;
    }
}
