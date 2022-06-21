package app.revanced.integrations.adremover;

public class HideSuggestionsPatch {

    //TODO: Create Patch
    //Not used yet
    public static void HideSuggestions(boolean showSuggestions) {
        AdRemoverAPI.removeSuggestions(showSuggestions);
    }

}
