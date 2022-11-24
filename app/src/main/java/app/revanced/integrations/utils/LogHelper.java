package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    /**
     * Allows creating log messages using lambdas.
     *
     * ie:
     * <code>
     *     debug(()->("something happened to id: " variable + " at time: " + time));
     * </code>
     *
     * Performance is improved since the log message is constructed only if logging is actually enabled.
     */
    public interface LogMessage {
        public String buildStringMessage();
    }

    /**
     * Calls {@link #debug(Class, LogMessage)} with the most outer class name of the code that is calling this method.
     * If you wish to log under a different class, instead directly call {@link #debug(Class, LogMessage)}
     */
    public static void debug(LogMessage message) {
        debug(ReVancedUtils.findOuterClassSimpleName(message.getClass()), message);
    }
    public static void debug(Class clazz, LogMessage message) {
        debug(clazz.getSimpleName(), message);
    }
    private static void debug(String simpleClassName, LogMessage message) { // must be private
        if (SettingsEnum.DEBUG.getBoolean()) {
            Log.d("revanced: " + simpleClassName, message.buildStringMessage());
        }
    }


    /**
     * Calls {@link #printException(Class, LogMessage)}
     * with the most outer class name of the code that is calling this method.
     * If you wish to log under a different class, instead directly call {@link #printException(Class, LogMessage)}
     */
    public static void printException(LogMessage message) {
        printException(ReVancedUtils.findOuterClassSimpleName(message.getClass()), message);
    }
    public static void printException(Class clazz, LogMessage message) {
        printException(clazz.getSimpleName(), message);
    }
    private static void printException(String simpleClassName, LogMessage message) { // must be private
        Log.e("revanced: " + simpleClassName, message.buildStringMessage());
    }



    /**
     * Calls {@link #printException(Class, LogMessage, Throwable)}
     * with the most outer class name of the code that is calling this method.
     * If you wish to log under a different class, instead directly call {@link #printException(Class, LogMessage, Throwable)}
     */
    public static void printException(LogMessage message, Throwable ex) {
        printException(ReVancedUtils.findOuterClassSimpleName(message.getClass()), message, ex);
    }
    public static void printException(Class clazz, LogMessage message, Throwable ex) {
        printException(clazz.getSimpleName(), message, ex);
    }
    private static void printException(String simpleClassName, LogMessage message, Throwable ex) { // must be private
        Log.e("revanced: " + simpleClassName, message.buildStringMessage(), ex);
    }


    /**
     * Calls {@link #info(Class, LogMessage)}
     * with the most outer class name of the code that is calling this method.
     * If you wish to log under a different class, instead directly call {@link #info(Class, LogMessage)}
     */
    public static void info(LogMessage message) {
        info(ReVancedUtils.findOuterClassSimpleName(message.getClass()), message);
    }
    public static void info(Class clazz, LogMessage message) {
        info(clazz.getSimpleName(), message);
    }
    private static void info(String simpleClassName, LogMessage message) { // must be private
        Log.i("revanced: " + simpleClassName, message.buildStringMessage());
    }



    //
    // deprecated slow methods.  Should eventually delete these methods
    //

    /**
     * Deprecated.  Instead call {@link #debug(LogMessage)},
     *              which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void debug(Class clazz, String message) {
        debug(clazz, ()->(message));
    }
    /**
     * Deprecated.  Instead call {@link #printException(Class, LogMessage, Throwable)}
     *              or {@link #printException(Class, LogMessage)}
     *              which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void printException(Class clazz, String message, Throwable ex) {
        printException(clazz, ()->(message), ex);
    }
    /**
     * Deprecated.  Instead call {@link #printException(Class, LogMessage)},
     *              which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void printException(Class clazz, String message) {
        printException(clazz, ()->(message));
    }
    /**
     * Deprecated.  Instead call {@link #info(Class, LogMessage)}
     *              or {@link #info(Class, LogMessage)}
     *              which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void info(Class clazz, String message) {
        info(clazz, ()->(message));
    }
}
