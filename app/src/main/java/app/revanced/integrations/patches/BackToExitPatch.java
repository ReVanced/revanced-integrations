package app.revanced.integrations.patches;

import com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity;
import app.revanced.integrations.utils.LogHelper;

public class BackToExitPatch {
    private static long backPressedCount = 0;

    /**
     * State whether the scroll position reaches the top.
     */
    public static boolean isHomeScrolledTop = false;

    /**
     * Handle the event of clicking the back button.
     *
     * @param activity The activity, the app is launched with to finish.
     */
    public static void exitOnBackPressed(WatchWhileActivity activity) {
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
     * Handle the event, when it is being scrolled.
     */
    public static void onStartScrollView() {
        isHomeScrolledTop = false;

        backPressedCount = 0;

        LogHelper.printDebug(() -> "Started scrolling");
    }

    /**
     * Handle the event of stopping to scroll.
     */
    public static void onStopScrollView() {
        isHomeScrolledTop = true;

        LogHelper.printDebug(() -> "Stopped scrolling");
    }
}
