package app.revanced.integrations.patches;

import android.widget.Toast;

import java.util.List;
import java.util.Map;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ConnectionFailureAlertPatch {
    /**
     * Minimum amount of time to wait before showing another dialog
     */
    private static final long MINIMUM_MINUTES_BETWEEN_DISPLAYING_ALERT_DIALOG = 30;

    /**
     * Last time an alert dialog was shown, or zero if never displayed
     */
    private static volatile long lastTimeAlertDialogDisplayed; // must be volatile or use synchronization

    /**
     * Injection point.  Runs off the main thread.
     *
     * @param statusCode HTTP status code of the completed YouTube connection
     * @param  urlConnectionHeaders all connection headers of the completed connection
     */
    public static void connectionCompleted(int statusCode, Map<String, List<String>> urlConnectionHeaders) {
        try {
            final boolean debugAlertDialog = false; // force the alert to show

            if ((statusCode >= 400 && statusCode < 500) || debugAlertDialog) {
                final long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis < lastTimeAlertDialogDisplayed + MINIMUM_MINUTES_BETWEEN_DISPLAYING_ALERT_DIALOG * 60 * 1000) {
                    return; // too soon to show again
                }
                lastTimeAlertDialogDisplayed = currentTimeMillis;

                ReVancedUtils.runOnMainThread(() -> {
                    // FIXME: display an alert, or a custom view explaining the situation
                    // but that requires an activity (which is currently not available)

                    // placeholder toast
                    Toast.makeText(ReVancedUtils.getContext(), "YouTube rejected the connection (try using incognito mode, install root ReVanced, blah blah blah)" , Toast.LENGTH_LONG).show();
                });
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "connectionCompleted failure", ex);
        }
    }
}
