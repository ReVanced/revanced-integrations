package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    public interface LogMessage {
        public String messageString();
    }

    //ToDo: Get Calling classname using Reflection

    /**
     * Only to be used for logging static strings that do not change.
     *
     * If using dynamically generated strings, such as:
     * <code>debug(Whatever.class, "loading video " + videoId)</code><br>
     * then instead call {@link #debug(Class, LogMessage)} with a lambda as the log message.
     */
    public static void debug(Class clazz, String message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            Log.d("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
        }
    }

    /**
     * Allows using Java 8 lambdas to prevent generating debug strings when debugging not enabled
     */
    public static void debug(Class clazz, LogMessage message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            Log.d("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message.messageString());
        }
    }

    public static void printException(Class clazz, String message, Throwable ex) {
        Log.e("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message, ex);
    }

    public static void printException(Class clazz, String message) {
        Log.e("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }

    public static void info(Class clazz, String message) {
        Log.i("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }
}
