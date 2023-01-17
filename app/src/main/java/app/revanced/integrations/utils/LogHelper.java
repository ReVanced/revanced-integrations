package app.revanced.integrations.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    /**
     * Log messages using lambdas.
     */
    public interface LogMessage {
        String buildMessageString();

        /**
         * @return For non inner classes, this returns {@link Class#getSimpleName()}.
         * For inner classes (static and anonymous), this returns the enclosing class simple name.<br>
         * <br>
         * For example, each of these classes return 'SomethingView':<br>
         * com.company.SomethingView<br>
         * com.company.SomethingView$StaticClass<br>
         * com.company.SomethingView$1<br>
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

    private static final String REVANCED_LOG_PREFIX = "revanced: ";

    /**
     * Logs information messages with the most outer class name of the code that is calling this method.
     */
    public static void printInfo(LogMessage message) {
        Log.i(REVANCED_LOG_PREFIX + message.findOuterClassSimpleName(), message.buildMessageString());
    }

    /**
     * Logs debug messages with the most outer class name of the code that is calling this method.
     * Whenever possible, the log string should be constructed entirely inside {@link LogMessage#buildMessageString()}
     * so the performance cost of building strings is paid only if {@link SettingsEnum#DEBUG} is enabled.
     */
    public static void printDebug(LogMessage message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            var messageString = message.buildMessageString();

            if (SettingsEnum.DEBUG_STACKTRACE.getBoolean()) {
                var builder = new StringBuilder(messageString);
                var sw = new StringWriter();
                new Throwable().printStackTrace(new PrintWriter(sw));

                builder.append(String.format("\n%s", sw));
                messageString = builder.toString();
            }

            Log.d(REVANCED_LOG_PREFIX + message.findOuterClassSimpleName(), messageString);
        }
    }

    /**
     * Logs messages with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message) {
        printException(message, null, null);
    }

    /**
     * Logs exceptions with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message, Throwable ex) {
        printException(message, ex, null);
    }

    /**
     * @param message          log message
     * @param ex               optional exception
     * @param userToastMessage optional toast message to display to user.
     *                         If not null, the toast is displayed regardless of
     *                         {@link SettingsEnum#DEBUG_SHOW_TOAST_ON_EXCEPTION} status.
     */
    public static void printException(@NonNull LogMessage message, @Nullable Throwable ex,
                                      @Nullable String userToastMessage) {
        String messageString = message.buildMessageString();
        String outerClassSimpleName = message.findOuterClassSimpleName();
        String logMessage = REVANCED_LOG_PREFIX + outerClassSimpleName;
        if (ex == null) {
            Log.e(logMessage, messageString);
        } else {
            Log.e(logMessage, messageString, ex);
        }
        if (userToastMessage != null || SettingsEnum.DEBUG_SHOW_TOAST_ON_EXCEPTION.getBoolean()) {
            String toastMessageToDisplay = (userToastMessage != null)
                    ? userToastMessage
                    : outerClassSimpleName + ": " + messageString;
            ReVancedUtils.runOnMainThread(() -> {
                Context context = ReVancedUtils.getContext();
                if (context != null) {
                    Toast.makeText(context, toastMessageToDisplay, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}