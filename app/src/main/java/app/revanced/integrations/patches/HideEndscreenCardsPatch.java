package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideEndscreenCardsPatch {
    //Used by app.revanced.patches.youtube.layout.hideendscreencards.resource.patch.HideEndscreenCardsPatch
    public static void HideEndscreen(View view) {
        if (!SettingsEnum.ENDSCREEN_CARDS_REMOVAL.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

}