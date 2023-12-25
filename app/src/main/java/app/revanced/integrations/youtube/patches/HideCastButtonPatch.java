package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Setting;

public class HideCastButtonPatch {

    // Used by app.revanced.patches.youtube.layout.castbutton.patch.HideCastButonPatch
    public static int getCastButtonOverrideV2(int original) {
        return Setting.HIDE_CAST_BUTTON.getBoolean() ? View.GONE : original;
    }
}
