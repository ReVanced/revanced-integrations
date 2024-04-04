package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class HideShowMoreButtonPatch {

    private static final Boolean HIDE_SHOW_MORE_BUTTON_ENABLED = Settings.HIDE_SHOW_MORE_BUTTON.get();

    private static boolean shouldHideShowMoreButton() {
        return HIDE_SHOW_MORE_BUTTON_ENABLED
                && NavigationBar.isSearchBarActive()
                // Search bar can be active but behind the player.
                && !PlayerType.getCurrent().isMaximizedOrFullscreen();
    }

    /**
     * Injection point.
     */
    public static void hideShowMoreButton(View view){
        if (shouldHideShowMoreButton()) {
            Utils.hideViewByLayoutParams(view);
        }
    }
}
