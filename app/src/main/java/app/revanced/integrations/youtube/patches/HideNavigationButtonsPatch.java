package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;

@SuppressWarnings("unused")
public final class HideNavigationButtonsPatch {
    private static final Boolean HIDE_HOME_BUTTON = Settings.HIDE_HOME_BUTTON.get();
    private static final Boolean HIDE_CREATE_BUTTON = Settings.HIDE_CREATE_BUTTON.get();
    private static final Boolean HIDE_SHORTS_BUTTON = Settings.HIDE_SHORTS_BUTTON.get();
    private static final Boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean switchCreateWithNotificationButton() {
        return SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON;
    }

    public static void navigationTabCreated(NavigationBar.NavigationButton button, View tabView) {
        final boolean shouldHide;
        switch (button) {
            case HOME:
                shouldHide = HIDE_HOME_BUTTON;
                break;
            case SHORTS:
                shouldHide = HIDE_SHORTS_BUTTON;
                break;
            case CREATE:
                shouldHide = HIDE_CREATE_BUTTON;
                break;
            default:
                return;
        }
        if (shouldHide) {
            tabView.setVisibility(View.GONE);
        }
    }
}
