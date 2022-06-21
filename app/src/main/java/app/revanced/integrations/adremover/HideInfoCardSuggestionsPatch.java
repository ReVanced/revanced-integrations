package app.revanced.integrations.adremover;

public class HideInfoCardSuggestionsPatch {

    //TODO: Create Patch
    //Not used yet
    public static void HideInfoCardSuggestions(Object InfoCardOverlayPresenter) {
        AdRemoverAPI.removeInfoCardSuggestions(InfoCardOverlayPresenter);
    }
}
