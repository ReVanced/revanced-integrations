package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Setting;

public class HideEndscreenCardsPatch {
    //Used by app.revanced.patches.youtube.layout.hideendscreencards.bytecode.patch.HideEndscreenCardsPatch
    public static void hideEndscreen(View view) {
        if (!Setting.HIDE_ENDSCREEN_CARDS.getBoolean()) return;
        view.setVisibility(View.GONE);
    }
}