package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideBreakingNewsPatch {

    /**
     * When spoofing to app versions older than 17.30.35, the watch history preview bar uses
     * the same layout components as the breaking news shelf.
     *
     * Don't apply the hide patch when using spoofing to these old versions.
     * Breaking news does not appear to be present in these older versions anyways.
     */
    private static boolean isSpoofingOldVersionWithHorizontalCardListWatchHistory() {
        return SettingsEnum.SPOOF_APP_VERSION.getBoolean()
                && SettingsEnum.SPOOF_APP_VERSION_TARGET.getString().compareTo("17.30.35") < 0;
    }

    public static void hideBreakingNews(View view) {
        if (!SettingsEnum.HIDE_BREAKING_NEWS.getBoolean()
                || isSpoofingOldVersionWithHorizontalCardListWatchHistory()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
