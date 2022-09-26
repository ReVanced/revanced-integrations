package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HidePromotedVideoItemPatch {
    //Used by app.revanced.patches.youtube.ad.general.patch.GeneralPatch
    public static void hidePromotedVideoItem(View view) {
        if (SettingsEnum.ADREMOVER_AD_REMOVAL.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }
}
