package app.revanced.integrations.adremover;

import android.view.View;

import app.revanced.integrations.settings.Settings;
import app.revanced.integrations.settings.SettingsEnum;

public class HideHomeAdsPatch {

    /**
     * Used by package app.revanced.extensions.Extensions
     * @param view
     */
    public static void HideHomeAds(View view) {
        if (!SettingsEnum.HOME_ADS_SHOWN_BOOLEAN.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }

}
