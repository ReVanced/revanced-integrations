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

    //
    // Search bar
    //

    private static volatile WeakReference<View> searchBarResultsRef = new WeakReference<>(null);

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

    //
    // Navigation bar buttons
    //

    /**
     * How long to wait for the set nav button latch to be released.  Maximum wait time must
     * be as small as possible while still allowing enough time for the nav bar to update.
     *
     * YT calls it's back button handlers out of order, and litho starts
     * filtering the previous screen before the navigation bar is updated.
     *
     * Fixing this situation and not needlessly wait requires somehow detecting if a back button
     * key-press will cause a tab change. Typically after pressing the back button, the time
     * between the first litho event and the when the nav button is updated is about 10-20ms.
     *
     * Using 50-100ms here should be enough time and not noticeable, since YT typically takes
     * 100-200ms (or more) just to update the view anyways.
     *
     * This issue can also be avoided on a patch by patch basis, by avoiding calls to
     * {@link NavigationButton#getSelectedNavigationButton()} unless absolutely necessary.
     */
    private static final long LATCH_AWAIT_TIMEOUT_MILLISECONDS = 50;

    /**
     * Used as a workaround to fix the issue of YT calling back button handlers out of order.
     * Used to hold calls to {@link NavigationButton#getSelectedNavigationButton()}
     * until the current navigation button can be determined.
     */
    @Nullable
    private static volatile CountDownLatch navButtonLatch;

    static {
        // On app startup litho can start before the navigation bar is initialized.
        // Force it to wait until the nav bar is updated.
        createNavButtonLatch();
    }

    private static void createNavButtonLatch() {
        navButtonLatch = new CountDownLatch(1);
    }

    private static void releaseNavButtonLatch() {
        CountDownLatch latch = navButtonLatch;
        if (latch != null) {
            navButtonLatch = null;
            latch.countDown();
        }
    }

    private static void waitForNavButtonLatchIfNeed() {
        CountDownLatch latch = navButtonLatch;
        if (latch == null) {
            return;
        }

        if (Utils.isCurrentlyOnMainThread()) {
            // The latch is released from the main thread, and waiting from the main thread will always timeout.
            // This situation has only been observed when navigating out of a submenu and not changing tabs.
            // and for those cases the nav bar did not change anyways so it's safe to return.
            Logger.printDebug(() -> "Cannot block main thread waiting for nav button. Using last known navbar button status.");
            return;
        }

        try {
            Logger.printDebug(() -> "Waiting for navbar button latch");
            if (latch.await(LATCH_AWAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)) {
                Logger.printDebug(() -> "Latch waiting complete");
                return;
            }

            // Timeout occurred, and a normal event when pressing the physical back button
            // does not change navigation tabs.
            releaseNavButtonLatch(); // Prevent other threads from waiting for no reason.
            Logger.printDebug(() -> "Latch wait timed out");

        } catch (InterruptedException ex) {
            Logger.printException(() -> "Wait interrupted", ex); // Will never happen.
        }
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
                if (button.ytEnumName.equals(lastEnumName)) {
                    Logger.printDebug(() -> "navigationTabLoaded: " + lastEnumName);
                    button.buttonLayoutRef = new WeakReference<>(navigationButtonGroup);
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
                View buttonView = button.buttonLayoutRef.get();

                if (buttonView == navButtonImageView) {
                    if (isSelected) {
                        NavigationButton.selectedNavigationButton = button;
                        // Wake up any threads waiting to return the currently selected nav button.
                        releaseNavButtonLatch();

                        Logger.printDebug(() -> "Changed to navigation button: " + button);
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
         * <b>Due to issues with how YT processes hardware back button events,
         * this patch uses workarounds that can cause this method to take up to 50ms.</b>
         *
         * @return The active navigation tab.
         *         If the user is in the upload video UI, this returns tab currently selected
         *         on screen (whatever tab the user was on before tapping the upload nav button).
         */
        @Nullable
        public static NavigationButton getSelectedNavigationButton() {
            waitForNavButtonLatchIfNeed();
            return selectedNavigationButton;
        }

        /**
         * YouTube enum name for this tab.
         */
        private final String ytEnumName;
        private volatile WeakReference<View> buttonLayoutRef = new WeakReference<>(null);

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
