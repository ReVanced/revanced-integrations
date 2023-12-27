package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

public class HideCrowdfundingBoxPatch {
    //Used by app.revanced.patches.youtube.layout.hidecrowdfundingbox.patch.HideCrowdfundingBoxPatch
    public static void hideCrowdfundingBox(View view) {
        if (!Settings.HIDE_CROWDFUNDING_BOX.getBoolean()) return;
        Utils.hideViewByLayoutParams(view);
    }
}
