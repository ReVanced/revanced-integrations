package app.revanced.integrations.adremover;


import android.view.View;

public class HideShortsButtonPatch {

    //Todo: Switch BooleanPreferences to Settings class
    //Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        AdRemoverAPI.hideShortsButton(view);
    }

    //Needed for the ShortsButtonRemoverPatch
    public static Enum lastPivotTab;
}
