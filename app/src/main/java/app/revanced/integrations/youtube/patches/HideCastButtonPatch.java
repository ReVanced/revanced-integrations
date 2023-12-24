package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class HideCastButtonPatch {

    // Used by app.revanced.patches.youtube.layout.castbutton.patch.HideCastButonPatch
    public static int getCastButtonOverrideV2(int original) {
        return SettingsEnum.HIDE_CAST_BUTTON.getBoolean() ? View.GONE : original;
    }
}
