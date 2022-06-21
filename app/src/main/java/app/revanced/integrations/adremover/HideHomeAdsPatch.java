package app.revanced.integrations.adremover;

import android.view.View;

import app.revanced.integrations.settings.Settings;

public class HideHomeAdsPatch {

    /**
     * Used by package app.revanced.extensions.Extensions
     * @param view
     */
    public static void HideHomeAds(View view) {
        if (!Settings.isHomeAdsShown()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }

}
