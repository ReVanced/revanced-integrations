package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!SettingsEnum.HIDE_LOAD_MORE_BUTTON.getBoolean()) return;
        ReVancedUtils.hideViewByLayoutParams(view);
    }
}
