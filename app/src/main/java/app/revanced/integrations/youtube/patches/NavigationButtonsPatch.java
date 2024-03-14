package app.revanced.integrations.youtube.patches;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationButtonsPatch {
    private static final Boolean SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON
            = Settings.SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON.get();
    private static final Boolean HIDE_CREATE_BUTTON = Settings.HIDE_CREATE_BUTTON.get();

    /**
     * Last YT navigation enum loaded.  Not necessarily the active navigation tab.
     */
    private static volatile Enum lastYTAppNavigationEnum;

    private static volatile boolean searchbarIsActive;

    /**
     * Injection point.
     */
    public static void searchBarResultsViewLoaded(View searchbarResults) {
        searchbarResults.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            final boolean isActive = searchbarResults.getParent() != null;
            if (searchbarIsActive != isActive) {
                searchbarIsActive = isActive;
                Logger.printDebug(() -> "searchbarIsActive: " + isActive);
            }
        });
    }

    public static boolean isSearchBarActive() {
        return searchbarIsActive;
    }

    /**
     * Injection point.
     */
    public static void setLastAppNavigationEnum(Enum ytNavigationButtonEnum) {
        lastYTAppNavigationEnum = ytNavigationButtonEnum;
    }

    /**
     * Injection point.
     */
    public static void createTabLoaded(final View view) {
        view.setVisibility(HIDE_CREATE_BUTTON ? View.GONE : View.VISIBLE);
    }

    /**
     * Injection point.
     */
    public static boolean switchCreateWithNotificationButton() {
        return SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON;
    }

    /**
     * Injection point.
     */
    public static void navigationTabLoaded(final View navigationButtonGroup) {
        try {
            if (lastYTAppNavigationEnum == null) return;

            String lastYTEnumName = lastYTAppNavigationEnum.name();
            for (NavigationButton button : NavigationButton.values()) {
                if (button.ytEnumName.equals(lastYTEnumName)) {
                    if (button.shouldHide) navigationButtonGroup.setVisibility(View.GONE);

                    ImageView imageView = Utils.getChildView((ViewGroup) navigationButtonGroup,
                            true, view -> view instanceof ImageView);
                    if (imageView != null) {
                        Logger.printDebug(() -> "navigationTabLoaded: " + lastYTEnumName);
                        button.imageViewRef = new WeakReference<>(imageView);
                        return;
                    }
                }
            }
            // Log the unknown tab as exception level, only if debug is enabled.
            // This is because unknown tabs do no harm and it's only relevant to developers.
            if (Settings.DEBUG.get()) {
                Logger.printException(() -> "Unknown tab: " + lastYTEnumName
                        + " view: " + navigationButtonGroup.getClass());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "navigationTabLoaded failure", ex);
        }
    }

    public enum NavigationButton {
        HOME("PIVOT_HOME", Settings.HIDE_HOME_BUTTON.get()),
        SHORTS("TAB_SHORTS", Settings.HIDE_SHORTS_BUTTON.get()),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS", Settings.HIDE_SUBSCRIPTIONS_BUTTON.get()),
        /**
         * Notifications tab.  Only present when
         * {@link Settings#SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON} is active.
         */
        ACTIVITY("TAB_ACTIVITY", false),
        /**
         * Incognito mode Library/You tab.
         */
        INCOGNITO("INCOGNITO_CIRCLE", false),
        /**
         * Old library tab (pre 'You' layout).
         */
        VIDEO_LIBRARY("VIDEO_LIBRARY_WHITE", false);

        /**
         * @return The active navigation tab. If the library tab is active this returns NULL.
         */
        @Nullable
        public static NavigationButton getActiveNavigationButton() {
            for (NavigationButton button : values()) {
                if (button.isActive()) return button;
            }
            return null;
        }

        /**
         * YouTube enum name for this tab.
         */
        private final String ytEnumName;
        private final boolean shouldHide;
        private volatile WeakReference<ImageView> imageViewRef = new WeakReference<>(null);

        NavigationButton(String ytEnumName, boolean shouldHide) {
            this.ytEnumName = ytEnumName;
            this.shouldHide = shouldHide;
        }

        public boolean isActive() {
            ImageView view = imageViewRef.get();
            return view != null && view.isSelected();
        }
    }
}
