package app.revanced.integrations.adremover;

import android.view.View;

import app.revanced.integrations.settings.Settings;

public class HideReelsPatch {

    /**
     * Used by app.revanced.patches.youtube.layout.reels.patch.HideReelsPatch
     *
     * @param view
     */
    public static void HideReel(View view) {
        if (!Settings.isReelsShown()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }
}
