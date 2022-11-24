package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    /**
     * Message log using lambdas.
     * <p>
     * ie:
     * <code>
     * debug(() -> "something happened to id: " variable + " at time: " + time);
     * </code>
     * <p>
     */
    public interface LogMessage {
        String buildStringMessage();

        /**
         * @return For non inner classes, this returns {@link Class#getSimpleName()}.
         * For inner and classes (static and anonymous), this returns the enclosing class simple name.<br>
         * ie: java.util.AbstractMap returns 'AbstractMap'<br>
         * ie: java.util.AbstractMap$SimpleEntry returns 'AbstractMap'<br>
         * returns an empty string for null classes
         */
        private String findOuterClassSimpleName() {
            var self = this.getClass();

            String fullClassName = self.getName();
            final int dollarSignIndex = fullClassName.indexOf('$');
            if (dollarSignIndex == -1) {
                return self.getSimpleName(); // already an outer class
            }
            // else, class is inner class (static or anonymous)

            // parse the simple name full name
            // a class with no package returns index of -1, but incrementing gives index zero which is correct
            final int simpleClassNameStartIndex = fullClassName.lastIndexOf('.') + 1;
            return fullClassName.substring(simpleClassNameStartIndex, dollarSignIndex);
        }
    }

    /**
     * Logs information messages with the most outer class name of the code that is calling this method.
     */
    public static void printInfo(LogMessage message) { // must be private
        Log.i("ReVanced: " + message.findOuterClassSimpleName(), message.buildStringMessage());
    }

    /**
     * Logs debug messages with the most outer class name of the code that is calling this method.
     */
    public static void printDebug(LogMessage message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            Log.d("ReVanced: " + message.findOuterClassSimpleName(), message.buildStringMessage());
        }
    }

    /**
     * Logs messages with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message) {
        Log.e("ReVanced: " + message.findOuterClassSimpleName(), message.buildStringMessage());
    }

    /**
     * Logs exceptions with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message, Throwable ex) {
        Log.e("ReVanced: " + message.findOuterClassSimpleName(), message.buildStringMessage(), ex);
    }

    /**
     * Deprecated. Instead call {@link #printDebug(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     * <p>
     * change existing code from:
     * <code>
     * debug(Whatever.class, "something happened to id: " variable + " at time: " + time);
     * </code>
     * <p>
     * into:
     * <code>
     * debug(()->"something happened to id: " variable + " at time: " + time);
     * </code>
     * <p>
     * <p>
     * TODO: to quickly change an entire project , change the deprecated code below from:
     *      <code>debug(clazz, ()->message);</code>
     * into:
     *      <code>debug(()->message);</code>
     *  then do Android Studio->Refactor->Inline  on this method
     *  and the entire project is now change.
     * <p>
     * A few places will need to be manually edited, such as string that could generate a checked exception):
     * <code>debug(clazz, "url connection response code " + connection.getResponseCode());</code>
     * <p>
     * but that's usually easy to fix by extracting the code that could generate a checked exception
     * <code>int responseCode = connection.getResponseCode();
     * debug(()->"url connection response code " + responseCode);</code>
     */
    @Deprecated
    public static void debug(Class _clazz, String message) {
        printDebug(() -> message);
    }

    /**
     * Deprecated.  Instead call {@link #printException(LogMessage, Throwable)}
     * or {@link #printException(LogMessage)}
     * which does not cause log messages to be constructed unless logging is enabled.<br>
     * <p>
     * change existing code from:
     * <code>
     * printException(Whatever.class, "something happened to id: " variable + " at time: " + time, ex);
     * </code>
     * <p>
     * into:
     * <code>
     * printException(()->"something happened to id: " variable + " at time: " + time, ex);
     * </code>
     */
    @Deprecated
    public static void printException(Class _clazz, String message, Throwable ex) {
        printException(() -> (message), ex);
    }

    /**
     * Deprecated. Instead call {@link #printException(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     * <p>
     * change existing code from:
     * <code>
     * printException(Whatever.class, "something happened to id: " variable + " at time: " + time);
     * </code>
     * <p>
     * into:
     * <code>
     * printException(()->"something happened to id: " variable + " at time: " + time);
     * </code>
     */
    @Deprecated
    public static void printException(Class _clazz, String message) {
        printException(() -> (message));
    }

    /**
     * Deprecated. Instead call {@link #printInfo(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     * <p>
     * change existing code from:
     * <code>
     * printInfo(Whatever.class, "something happened to id: " variable + " at time: " + time);
     * </code>
     * <p>
     * into:
     * <code>
     * printInfo(()->"something happened to id: " variable + " at time: " + time);
     * </code>
     */
    @Deprecated
    public static void info(Class _clazz, String message) {
        printInfo(() -> (message));
    }
}
