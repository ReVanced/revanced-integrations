package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Setting;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public class HideCrowdfundingBoxPatch {
    //Used by app.revanced.patches.youtube.layout.hidecrowdfundingbox.patch.HideCrowdfundingBoxPatch
    public static void hideCrowdfundingBox(View view) {
        if (!Setting.HIDE_CROWDFUNDING_BOX.getBoolean()) return;
        ReVancedUtils.hideViewByLayoutParams(view);
    }
}
