package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideEndScreenPatch {
    //Used by app.revanced.patches.youtube.ad.general.patch.GeneralPatch
    public static void hide(View view) {
        if (SettingsEnum.ENDSCREEN_SHOWN.getBoolean()) return;
        view.setVisibility(View.GONE);
    }
}
