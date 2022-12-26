package app.revanced.integrations.patches;

import com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class DouBleBackToClosePatch {
    /**
     * Time between two back button presses
     */
    private static final long PRESSED_TIMEOUT_MILLISECONDS = 1000;

    /**
     * Last time back button was pressed
     */
    private static long lastTimeBackPressed = 0;

    /**
     * State whether scroll position reaches the top
     */
    private static boolean isScrollTop = false;

    /**
     * Detect event when back button is pressed
     *
     * @param activity is used when closing the app
     */
    public static void closeActivityOnBackPressed(WatchWhileActivity activity) {
        // Check scroll position reaches the top in home feed
        if (!isScrollTop
                || !SettingsEnum.DOUBLE_BACK_TO_CLOSE.getBoolean()) {
            return;
        }

        // If the time between two back button presses does not reach PRESSED_TIMEOUT_MILLISECONDS,
        // set lastTimeBackPressed to the current time.
        if (System.currentTimeMillis() - lastTimeBackPressed >= PRESSED_TIMEOUT_MILLISECONDS) {
            lastTimeBackPressed = System.currentTimeMillis();
            LogHelper.printDebug(() -> "BackPressed detected");
        } else {
            LogHelper.printDebug(() -> "Activity is finished");
            activity.finish();
        }
    }

    /**
     * Detect event when ScrollView is created by RecyclerView
     *
     * @param start of ScrollView
     */
    public static void onStartScrollView() {
        isScrollTop = false;
        LogHelper.printDebug(() -> "ScrollView is created");
    }

    /**
     * Detect event when the scroll position reaches the top by the back button
     *
     * @param stop of ScrollView
     */
    public static void onStopScrollView() {
        isScrollTop = true;
        LogHelper.printDebug(() -> "ScrollView is stopped");
    }
}
