package app.revanced.integrations.youtube.shared;

import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton.CREATE;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationBar {

    private static volatile WeakReference<View> searchBarResultsRef = new WeakReference<>(null);

    /**
     * When using the back button and the navigation button changes, the button is updated
     * a few milliseconds after litho starts creating the view.  To fix this, any thread
     * calling for the current navigation button waits until this latch is released.
     *
     * The latch is also initial set, because on app startup litho can start before the navigation bar is initialized.
     */
    @Nullable
    private static volatile CountDownLatch navButtonLatch;

    static {
        createNavButtonLatch();
    }

    private static void createNavButtonLatch() {
        navButtonLatch = new CountDownLatch(1);
    }

    private static void releaseNavButtonLatch() {
        CountDownLatch latch = navButtonLatch;
        if (latch != null) {
            latch.countDown();
        }
        navButtonLatch = null;
    }

    private static boolean waitForLatchIfNeed() {
        CountDownLatch latch = navButtonLatch;
        if (latch == null) {
            return true;
        }

        try {
            Logger.printDebug(() -> "Waiting for navbar button latch");
            if (latch.await(1000, TimeUnit.MILLISECONDS)) {
                Logger.printDebug(() -> "Waiting complete");
                return true;
            }
            Logger.printDebug(() -> "Get navigation button wait timed out");
            navButtonLatch = null;
        } catch (InterruptedException ex) {
            Logger.printException(() -> "Wait interrupted", ex); // Will never happen.
        }

        return false;
    }

    /**
     * Injection point.
     */
    public static void searchBarResultsViewLoaded(View searchbarResults) {
        searchBarResultsRef = new WeakReference<>(searchbarResults);
    }

    /**
     * @return If the search bar is on screen.  This includes if the player
     *         is on screen and the search results are behind the player (and not visible).
     *         Detecting the search is covered by the player can be done by checking {@link PlayerType#isMaximizedOrFullscreen()}.
     */
    public static boolean isSearchBarActive() {
        View searchbarResults = searchBarResultsRef.get();
        return searchbarResults != null && searchbarResults.getParent() != null;
    }

    /**
     * Last YT navigation enum loaded.  Not necessarily the active navigation tab.
     */
    @Nullable
    private static volatile String lastYTNavigationEnumName;

    /**
     * Injection point.
     */
    public static void setLastAppNavigationEnum(@Nullable Enum<?> ytNavigationEnumName) {
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
                if (button.ytEnumName.equals(lastEnumName)) {;
                    Logger.printDebug(() -> "navigationTabLoaded: " + lastEnumName);
                    button.imageViewRef = new WeakReference<>(navigationButtonGroup);
                    navigationTabCreatedCallback(button, navigationButtonGroup);
                    return;
                }
            }

            // Log the unknown tab as exception level, only if debug is enabled.
            // This is because unknown tabs do no harm, and it's only relevant to developers.
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
     * Unique hook just for the 'Create' and 'You' tab.
     */
    public static void navigationImageResourceTabLoaded(View view) {
        // 'You' tab has no YT enum name and the enum hook is not called for it.
        // Compare the last enum to figure out which tab this actually is.
        if (CREATE.ytEnumName.equals(lastYTNavigationEnumName)) {
            navigationTabLoaded(view);
        } else {
            lastYTNavigationEnumName = NavigationButton.LIBRARY_YOU.ytEnumName;
            navigationTabLoaded(view);
        }
    }

    /**
     * Injection point.
     */
    public static void navigationTabSelected(View navButtonImageView, boolean isSelected) {
        try {
            for (NavigationButton button : NavigationButton.values()) {
                View buttonView = button.imageViewRef.get();
                if (buttonView == navButtonImageView) {
                    if (isSelected) {
                        if (NavigationButton.selectedNavigationButton != button) {
                            Logger.printDebug(() -> "Changed to navigation button: " + button);
                            NavigationButton.selectedNavigationButton = button;
                        }

                        // Wake up any threads waiting to return the currently selected nav button.
                        releaseNavButtonLatch();

                    } else if (NavigationButton.selectedNavigationButton == button) {
                        NavigationButton.selectedNavigationButton = null;
                        Logger.printDebug(() -> "Navigated away from button: " + button);
                    }
                    return;
                }
            }

            if (BaseSettings.DEBUG.get()) {
                // An unknown tab was selected.  Only show a toast is debug mode is enabled.
                Logger.printException(() -> "Unknown navigation view selected: " + navButtonImageView);
            }
            NavigationButton.selectedNavigationButton = null;
        } catch (Exception ex) {
            Logger.printException(() -> "navigationTabSelected failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void onBackPressed(Activity activity) {
        Logger.printDebug(() -> "Back button pressed");
        createNavButtonLatch();
    }

    /** @noinspection EmptyMethod*/
    private static void navigationTabCreatedCallback(NavigationButton button, View tabView) {
        // Code is added during patching.
    }

    public enum NavigationButton {
        HOME("PIVOT_HOME"),
        SHORTS("TAB_SHORTS"),
        /**
         * Create new video tab.
         * This tab will never be in a selected state, even if the create video UI is on screen.
         */
        CREATE("CREATION_TAB_LARGE"),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS"),
        /**
         * Notifications tab.  Only present when
         * {@link Settings#SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON} is active.
         */
        NOTIFICATIONS("TAB_ACTIVITY"),
        /**
         * Library tab when the user is not logged in.
         */
        LIBRARY_LOGGED_OUT("ACCOUNT_CIRCLE"),
        /**
         * User is logged in with incognito mode enabled.
         */
        LIBRARY_INCOGNITO("INCOGNITO_CIRCLE"),
        /**
         * Old library tab (pre 'You' layout), only present when version spoofing.
         */
        LIBRARY_OLD_UI("VIDEO_LIBRARY_WHITE"),
        /**
         * 'You' library tab that is sometimes momentarily loaded.
         * When this is loaded, {@link #LIBRARY_YOU} is also present.
         *
         * This might be a temporary tab while the user profile photo is loading,
         * but its exact purpose is not entirely clear.
         */
        LIBRARY_PIVOT_UNKNOWN("PIVOT_LIBRARY"),
        /**
         * Modern library tab with 'You' layout.
         */
        // The hooked YT code does not use an enum, and a dummy name is used here.
        LIBRARY_YOU("YOU_LIBRARY_DUMMY_PLACEHOLDER_NAME");

        @Nullable
        private static volatile NavigationButton selectedNavigationButton;

        /**
         * This will return null only if the currently selected tab is unknown.
         * This scenario will only happen if the UI has different tabs due to an A/B user test
         * or YT abruptly changes the navigation layout for some other reason.
         *
         * All code calling this method should handle a null return value.
         *
         * @return The active navigation tab.
         *         If the user is in the upload video UI, this returns tab currently selected
         *         on screen (whatever tab the user was on before tapping the upload nav button).
         */
        @Nullable
        public static NavigationButton getSelectedNavigationButton() {
            if (waitForLatchIfNeed()) {
                return selectedNavigationButton;
            }
            return null; // Latch wait timed out, and it's unclear which tab is selected.
        }

        /**
         * YouTube enum name for this tab.
         */
        private final String ytEnumName;
        private volatile WeakReference<View> imageViewRef = new WeakReference<>(null);

        NavigationButton(String ytEnumName) {
            this.ytEnumName = ytEnumName;
        }

        public boolean isLibraryOrYouTab() {
            return this == LIBRARY_YOU || this == LIBRARY_PIVOT_UNKNOWN
                    || this == LIBRARY_OLD_UI || this == LIBRARY_INCOGNITO
                    || this == LIBRARY_LOGGED_OUT;
        }
    }
}
