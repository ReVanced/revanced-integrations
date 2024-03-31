package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.shared.NavigationBar;

@SuppressWarnings("unused")
public class HideLoadMoreButtonPatch {
    private static final boolean HIDE_LOAD_MORE_BUTTON_ENABLED =
            Settings.HIDE_LOAD_MORE_BUTTON.get() && NavigationBar.isSearchBarActive();
    
    public static void hideLoadMoreButton(View view){
        if(!HIDE_LOAD_MORE_BUTTON_ENABLED) return;
        
        Utils.hideViewByLayoutParams(view);
    }
}
