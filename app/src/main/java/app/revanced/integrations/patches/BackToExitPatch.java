package app.revanced.integrations.patches;

import com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity;
import app.revanced.integrations.utils.LogHelper;

public class BackToExitPatch {
    private static long backPressedCount = 0;

    /**
     * State whether scroll position reaches the top
     */
    public static boolean isHomeScrolledTop = false;

    /**
     * Detect event when back button is pressed
     *
     * @param activity is used when closing the app
     */
    public static void exitOnBackPressed(WatchWhileActivity activity) {
        // Check scroll position reaches the top in home feed
        if (!isHomeScrolledTop) return;

        // Close the current activity once counter reached 1
        if (backPressedCount <= 0) {
            LogHelper.printDebug(() -> "BackPressed detected");

            backPressedCount++;
        } else {
            LogHelper.printDebug(() -> "Activity is finished");

            activity.finish();
        }
    }

    /**
     * Detect event when ScrollView is created by RecyclerView
     */
    public static void onStartScrollView() {
        isHomeScrolledTop = false;

        backPressedCount = 0;

        LogHelper.printDebug(() -> "ScrollView is created");
    }

    /**
     * Detect event when the scroll position reaches the top by the back button
     */
    public static void onStopScrollView() {
        isHomeScrolledTop = true;

        LogHelper.printDebug(() -> "ScrollView is stopped");
    }
}
