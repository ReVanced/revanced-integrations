package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardSuggestionsPatch {

    //TODO: Create Patch
    //Not used yet
    public static boolean ShowInfoCardSuggestions() {
        return SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.getBoolean();
    }
}
