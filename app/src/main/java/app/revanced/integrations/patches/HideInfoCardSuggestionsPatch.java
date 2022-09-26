package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardSuggestionsPatch {
    public static boolean hideInfoCardDrawerHeader() {
        return SettingsEnum.INFO_CARDS_SHOWN.getBoolean();
    }
    public static void hideInfoCardSuggestions(View view) {
        if (!SettingsEnum.INFO_CARDS_SHOWN.getBoolean()) {
            view.setVisibility(View.GONE);
        }
    }
}
