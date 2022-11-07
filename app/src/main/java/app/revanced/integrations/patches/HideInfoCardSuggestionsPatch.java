package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardSuggestionsPatch {
    public static void hideInfoCardIncognito(View view) {
        if (!SettingsEnum.INFO_CARDS_SHOWN.getBoolean()) {
            view.setVisibility(View.GONE);
        }
    }

    public static boolean hideInfoCard() {
        return !SettingsEnum.INFO_CARDS_SHOWN.getBoolean();
    }
}
