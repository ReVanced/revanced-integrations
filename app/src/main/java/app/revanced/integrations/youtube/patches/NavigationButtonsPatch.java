package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class NavigationButtonsPatch {
    private static final Map<NavigationBar.NavigationButton, Boolean> shouldHideMap = new HashMap<>() {
        {
            put(NavigationBar.NavigationButton.HOME, Settings.HIDE_HOME_BUTTON.get());
            put(NavigationBar.NavigationButton.CREATE, Settings.HIDE_CREATE_BUTTON.get());
            put(NavigationBar.NavigationButton.SHORTS, Settings.HIDE_SHORTS_BUTTON.get());
        }
    };

    private static final Boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean switchCreateWithNotificationButton() {
        return SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON;
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationBar.NavigationButton button, View tabView) {
        if (Boolean.TRUE.equals(shouldHideMap.get(button))) {
            tabView.setVisibility(View.GONE);
        }
    }
}
