package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideHomeAdsPatch {

    /**
     * Used by package app.revanced.extensions.Extensions
     *
     * @param view
     */
    public static void HideHomeAds(View view) {
        if (SettingsEnum.HOME_ADS_REMOVAL.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }

}
