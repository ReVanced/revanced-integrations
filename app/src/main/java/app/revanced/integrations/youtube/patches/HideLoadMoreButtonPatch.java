package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.shared.NavigationBar;

@SuppressWarnings("unused")
public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!shouldHideLoadMoreButton()) return;
        
        Utils.hideViewByLayoutParams(view);
    }

    private static boolean shouldHideLoadMoreButton() {
        return Settings.HIDE_LOAD_MORE_BUTTON.get() && 
            NavigationBar.isSearchBarActive();
    }
}
