package app.revanced.integrations.youtube.patches;


import android.view.View;
import app.revanced.integrations.youtube.settings.Settings;

public final class NavigationButtonsPatch {
    public static Enum lastNavigationButton;

    public static void hideCreateButton(final View view) {
        view.setVisibility(Settings.HIDE_CREATE_BUTTON.getBoolean() ? View.GONE : View.VISIBLE);
    }

    public static boolean switchCreateWithNotificationButton() {
        return Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.getBoolean();
    }

    public static void hideButton(final View buttonView) {
        if (lastNavigationButton == null) return;

        for (NavigationButton button : NavigationButton.values())
            if (button.name.equals(lastNavigationButton.name()))
                if (button.enabled) buttonView.setVisibility(View.GONE);
    }

    private enum NavigationButton {
        HOME("PIVOT_HOME", Settings.HIDE_HOME_BUTTON.getBoolean()),
        SHORTS("TAB_SHORTS", Settings.HIDE_SHORTS_BUTTON.getBoolean()),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS", Settings.HIDE_SUBSCRIPTIONS_BUTTON.getBoolean());
        private final boolean enabled;
        private final String name;

        NavigationButton(final String name, final boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }
    }
}
