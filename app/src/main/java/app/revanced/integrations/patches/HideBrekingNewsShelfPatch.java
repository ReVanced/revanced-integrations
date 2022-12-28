package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideBrekingNewsShelfPatch {
    //Used by app.revanced.patches.youtube.layout.hidebreakingnewsshelf.patch.HideBreakingNewsShelfPatch
    public static void hideBreakingNewsShelf(View view) {
        if (!SettingsEnum.HIDE_BREAKING_NEWS_SHELF.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
