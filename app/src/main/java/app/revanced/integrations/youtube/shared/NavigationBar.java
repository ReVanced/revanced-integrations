package app.revanced.integrations.youtube.shared;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationBar {
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
     * Last YT navigation enum loaded.  Not necessarily the active navigation tab.
     */
    private static volatile Enum lastYTAppNavigationEnum;

    /**
     * Injection point.
     */
    public static void setLastAppNavigationEnum(Enum ytNavigationButtonEnum) {
        lastYTAppNavigationEnum = ytNavigationButtonEnum;
    }

    /**
     * Injection point.
     *
     * Called for most, but not all, of the navigation tabs.
     */
    public static void navigationTabLoaded(final View navigationButtonGroup) {
        try {
            if (lastYTAppNavigationEnum == null) return;

            String lastYTEnumName = lastYTAppNavigationEnum.name();
            for (NavigationButton button : NavigationButton.values()) {
                if (button.ytEnumName.equals(lastYTEnumName)) {
                    ImageView imageView = Utils.getChildView((ViewGroup) navigationButtonGroup,
                            true, view -> view instanceof ImageView);
                    if (imageView != null) {
                        Logger.printDebug(() -> "navigationTabLoaded: " + lastYTEnumName);
                        button.imageViewRef = new WeakReference<>(imageView);
                        navigationTabCreatedCallback(button, navigationButtonGroup);
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

    /**
     * Injection point.
     *
     * Unique hook just for the create button.
     */
    public static void createTabLoaded(View view) {
        navigationTabCreatedCallback(NavigationButton.CREATE, view);
    }

    private static void navigationTabCreatedCallback(NavigationBar.NavigationButton button, View tabView) {
        // Code is added during patching.
    }

    public enum NavigationButton {
        HOME("PIVOT_HOME"),
        SHORTS("TAB_SHORTS"),
        /**
         * Create new video tab.
         *
         * {@link #isSelected()} always returns false, even if the create video UI is on screen.
         */
        CREATE("CREATION_TAB_LARGE"),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS"),
        /**
         * Notifications tab.  Only present when
         * {@link Settings#SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON} is active.
         */
        ACTIVITY("TAB_ACTIVITY"),
        /**
         * Incognito mode Library/You tab.
         */
        INCOGNITO("INCOGNITO_CIRCLE"),
        /**
         * Old library tab (pre 'You' layout).
         */
        VIDEO_LIBRARY("VIDEO_LIBRARY_WHITE");

        /**
         * @return The active navigation tab. If the library tab is selected this returns NULL.
         */
        @Nullable
        public static NavigationButton getActiveNavigationButton() {
            for (NavigationButton button : values()) {
                if (button.isSelected()) return button;
            }
            return null;
        }

        /**
         * YouTube enum name for this tab.
         */
        private final String ytEnumName;
        private volatile WeakReference<ImageView> imageViewRef = new WeakReference<>(null);

        NavigationButton(String ytEnumName) {
            this.ytEnumName = ytEnumName;
        }

        public boolean isSelected() {
            ImageView view = imageViewRef.get();
            return view != null && view.isSelected();
        }
    }
}
