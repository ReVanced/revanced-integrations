package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.Utils;

public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!SettingsEnum.HIDE_LOAD_MORE_BUTTON.getBoolean()) return;
        Utils.hideViewByLayoutParams(view);
    }
}
