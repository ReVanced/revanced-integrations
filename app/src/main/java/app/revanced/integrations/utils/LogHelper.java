package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    /**
     * Message log using lambdas.
     * <p>
     * ie:
     * <code>
     * printDebug(() -> "something happened to id: " variable + " at time: " + time);
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
            var selfClass = this.getClass();

            String fullClassName = selfClass.getName();
            final int dollarSignIndex = fullClassName.indexOf('$');
            if (dollarSignIndex == -1) {
                return selfClass.getSimpleName(); // already an outer class
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
    public static void printInfo(LogMessage message) {
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
     *
     * TODO: delete this method
     */
    @Deprecated
    public static void debug(Class _clazz, String message) {
        printDebug(() -> message);
    }

    /**
     * Deprecated.  Instead call {@link #printException(LogMessage, Throwable)}
     * which does not cause log messages to be constructed unless logging is enabled.
     *
     * TODO: delete this method
     */
    @Deprecated
    public static void printException(Class _clazz, String message, Throwable ex) {
        printException(() -> (message), ex);
    }

    /**
     * Deprecated. Instead call {@link #printException(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     *
     * TODO: delete this method
     */
    @Deprecated
    public static void printException(Class _clazz, String message) {
        printException(() -> (message));
    }

    /**
     * Deprecated. Instead call {@link #printInfo(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     *
     * TODO: delete this method
     */
    @Deprecated
    public static void info(Class _clazz, String message) {
        printInfo(() -> (message));
    }
}
