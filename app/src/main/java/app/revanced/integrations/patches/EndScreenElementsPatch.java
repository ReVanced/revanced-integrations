package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class EndScreenElementsPatch {
    //Used by app.revanced.patches.youtube.ad.general.patch.GeneralPatch
    public static void hideEndScreenElements(View view) {
        if (SettingsEnum.ENDSCREEN_ELEMENTS_REMOVAL.getBoolean()) {
            view.setVisibility(View.GONE);
        }
    }
}
