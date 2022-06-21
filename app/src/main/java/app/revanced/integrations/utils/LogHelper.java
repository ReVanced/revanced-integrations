package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.Settings;

public class LogHelper {

    public static void debug(String tag, String message) {
        if (Settings.isDebug()) {
            Log.d(tag, message);
        }
    }

    public static void printException(String tag, String message, Throwable ex) {
        LogHelper.printException(tag, message, ex);
    }

    public static void printException(String tag, String message) {
        LogHelper.printException(tag, message);
    }
}
