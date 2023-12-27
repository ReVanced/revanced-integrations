package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!Settings.HIDE_LOAD_MORE_BUTTON.getBoolean()) return;
        Utils.hideViewByLayoutParams(view);
    }
}
