package app.revanced.integrations.adremover;


import android.view.View;

import app.revanced.integrations.log.LogHelper;
import app.revanced.integrations.preferences.BooleanPreferences;
import app.revanced.integrations.settings.Settings;

public class HideShortsButtonPatch {

    //Todo: Switch BooleanPreferences to Settings class
    //Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        AdRemoverAPI.hideShortsButton(view);
    }
}
