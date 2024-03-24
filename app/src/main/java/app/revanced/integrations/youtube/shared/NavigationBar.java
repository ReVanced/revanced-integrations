package app.revanced.integrations.youtube.shared;

import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton.CREATE;

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
    @Nullable
    private static volatile String lastYTNavigationEnumName;

    /**
     * Injection point.
     */
    public static void setLastAppNavigationEnum(@Nullable Enum ytNavigationEnumName) {
        if (ytNavigationEnumName != null) {
            lastYTNavigationEnumName = ytNavigationEnumName.name();
        }
    }

    /**
     * Injection point.
     */
    public static void navigationTabLoaded(final View navigationButtonGroup) {
        try {
            String lastEnumName = lastYTNavigationEnumName;
            for (NavigationButton button : NavigationButton.values()) {
                if (button.ytEnumName.equals(lastEnumName)) {
                    ImageView imageView = Utils.getChildView((ViewGroup) navigationButtonGroup,
                            true, view -> view instanceof ImageView);
                    if (imageView != null) {
                        Logger.printDebug(() -> "navigationTabLoaded: " + lastEnumName);
                        button.imageViewRef = new WeakReference<>(imageView);
                        navigationTabCreatedCallback(button, navigationButtonGroup);
                        return;
                    }
                }
            }
            // Log the unknown tab as exception level, only if debug is enabled.
            // This is because unknown tabs do no harm and it's only relevant to developers.
            if (Settings.DEBUG.get()) {
                Logger.printException(() -> "Unknown tab: " + lastEnumName
                        + " view: " + navigationButtonGroup.getClass());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "navigationTabLoaded failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * Unique hook just for the create and 'You' tab.
     */
    public static void navigationImageResourceTabLoaded(View view) {
        // 'You' tab has no YT enum name and the enum hook is not called for it.
        // Compare the last enum to figure out which tab this actually is.
        if (CREATE.ytEnumName.equals(lastYTNavigationEnumName)) {
            navigationTabLoaded(view);
        } else {
            lastYTNavigationEnumName = NavigationButton.YOU_LIBRARY.ytEnumName;
            navigationTabLoaded(view);
        }
    }

    /** @noinspection EmptyMethod*/
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
        VIDEO_LIBRARY("VIDEO_LIBRARY_WHITE"),
        /**
         * 'You' library tab that is sometimes temporarily loaded.
         *
         * This likely is a temporary tab used while the user profile photo is loading,
         * but this is not entirely clear.
         */
        PIVOT_LIBRARY("PIVOT_LIBRARY"),
        /**
         * Modern library tab with 'You' layout.
         */
        // The hooked YT code does not use an enum, and a dummy name is used here.
        YOU_LIBRARY("YOU_LIBRARY_DUMMY_PLACEHOLDER_NAME");

        /**
         * @return The active navigation tab.
         *         If the user is in the create new video UI, this returns NULL.
         */
        @Nullable
        public static NavigationButton getActiveNavigationButton() {
            for (NavigationButton button : values()) {
                if (button.isSelected()) return button;
            }
            return null;
        }

        /**
         * @return If the currently selected tab is a 'You' or library type.
         *         Covers all known app states including incognito mode and version spoofing.
         */
        public static boolean libraryOrYouTabIsActive() {
            return YOU_LIBRARY.isSelected() || PIVOT_LIBRARY.isSelected()
                    || VIDEO_LIBRARY.isSelected() || INCOGNITO.isSelected();
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
