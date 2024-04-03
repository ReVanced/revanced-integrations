package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class HideLoadMoreButtonPatch {

    private static final Boolean HIDE_LOAD_MORE_BUTTON_ENABLED = Settings.HIDE_LOAD_MORE_BUTTON.get();

    private static boolean shouldHideLoadMoreButton() {
        return HIDE_LOAD_MORE_BUTTON_ENABLED
                // Search bar can be active but behind the player.
                && !PlayerType.getCurrent().isMaximizedOrFullscreen()
                && NavigationBar.isSearchBarActive();
    }

    public static void hideLoadMoreButton(View view){
        if (shouldHideLoadMoreButton()) {
            Utils.hideViewByLayoutParams(view);
        }
    }
}
