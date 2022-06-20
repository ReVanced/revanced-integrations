package app.revanced.integrations.adremover;

import android.util.Log;
import android.view.View;

import app.revanced.integrations.preferences.BooleanPreferences;
import app.revanced.integrations.settings.XGlobals;

public class HideCreateButton {

    public static void hideCreateButton(View view) {
        if (BooleanPreferences.isCreateButtonHidden()) {
            if (XGlobals.debug) {
                Log.d("XAdRemover", "Create button: shown");
            }
            view.setVisibility(View.GONE);
        } else if (XGlobals.debug) {
            Log.d("XAdRemover", "Create button: hidden");
        }
    }
}
