package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideBreakingNewsPatch {

    public static void hideBreakingNews(View view) {
        // Don't hide if spoofing to an old version, as the component was previously used for the watch history.
        if (!SettingsEnum.HIDE_BREAKING_NEWS.getBoolean() || SettingsEnum.SPOOF_APP_VERSION.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
