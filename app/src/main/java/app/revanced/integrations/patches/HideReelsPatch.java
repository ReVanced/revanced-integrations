package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

// edit: appears to be dead code
public class HideReelsPatch {

    /**
     * Used by app.revanced.patches.youtube.layout.reels.patch.HideReelsPatch
     *
     * @param view
     */
    public static void HideReel(View view) {
        if (SettingsEnum.HIDE_REEL_BUTTON.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }
}
