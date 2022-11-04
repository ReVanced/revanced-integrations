package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideEndscreenPatch {
    //Used by app.revanced.patches.youtube.ad.general.bytecode.patchGeneralBytecodeAdsPatch
    public static void HideEndscreen(View view) {
        if (!SettingsEnum.ENDSCREEN_CARDS_SHOWN.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

}
