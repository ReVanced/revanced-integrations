package app.revanced.integrations.adremover;

import android.util.Log;
import android.view.View;

import app.revanced.integrations.preferences.BooleanPreferences;
import app.revanced.integrations.settings.XGlobals;

public class HideShortsButton {

    public static void hideShortsButton(View view) {
        if (XGlobals.lastPivotTab != null && XGlobals.lastPivotTab.name() == "TAB_SHORTS") {
            if (BooleanPreferences.isShortsButtonHidden()) {
                if (XGlobals.debug) {
                    Log.d("XAdRemover", "Shorts button: shown");
                }
                view.setVisibility(View.GONE);
            } else if (XGlobals.debug) {
                Log.d("XAdRemover", "Shorts button: hidden");
            }
        }
    }
}
