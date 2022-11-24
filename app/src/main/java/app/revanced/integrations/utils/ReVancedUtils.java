package app.revanced.integrations.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import app.revanced.integrations.sponsorblock.player.PlayerType;

public class ReVancedUtils {

    private static PlayerType env;
    private static boolean newVideo = false;

    public static Context context;

    private ReVancedUtils() {} // utility class

    public static boolean containsAny(final String value, final String... targets) {
        for (String string : targets)
            if (!string.isEmpty() && value.contains(string)) return true;
        return false;
    }

    public static void setNewVideo(boolean started) {
        LogHelper.debug(ReVancedUtils.class, "New video started: " + started);
        newVideo = started;
    }

    public static boolean isNewVideoStarted() {
        return newVideo;
    }

    public static Integer getResourceIdByName(Context context, String type, String name) {
        try {
            Resources res = context.getResources();
            return res.getIdentifier(name, type, context.getPackageName());
        } catch (Throwable exception) {
            LogHelper.printException(ReVancedUtils.class, "Resource not found.", exception);
            return null;
        }
    }

    public static void setPlayerType(PlayerType type) {
        env = type;
    }

    public static PlayerType getPlayerType() {
        return env;
    }

    public static int getIdentifier(String name, String defType) {
        Context context = getContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static Context getContext() {
        if (context != null) {
            return context;
        } else {
            LogHelper.printException(ReVancedUtils.class, "Context is null, returning null!");
            return null;
        }
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    /**
     * @return For non inner classes, this returns {@link Class#getSimpleName()}.
     *         For inner and classes (static and anonymous), this returns the enclosing class simple name.<br>
     *         ie: java.util.AbstractMap returns 'AbstractMap'<br>
     *         ie: java.util.AbstractMap$SimpleEntry returns 'AbstractMap'<br>
     *         returns an empty string for null classes
     */
    public static String findOuterClassSimpleName(Class clazz) {
        if (clazz == null) return ""; // It might be better to throw an exception

        String fullClassName = clazz.getName();
        final int dollarSignIndex = fullClassName.indexOf('$');
        if (dollarSignIndex == -1) {
            return clazz.getSimpleName(); // already an outer class
        }
        // else, class is inner class (static or anonymous)

        // parse the simple name full name
        // a class with no package returns index of -1, but incrementing gives index zero which is correct
        final int simpleClassNameStartIndex = fullClassName.lastIndexOf('.') + 1;
        return fullClassName.substring(simpleClassNameStartIndex, dollarSignIndex);
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * @return if the calling thread is on the main thread
     */
    public static boolean currentIsOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }
    /**
     * @throws IllegalStateException if the calling thread is _not_ on the main thread
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (! currentIsOnMainThread()) {
            throw new IllegalStateException("must call _on_ the main thread");
        }
    }
    /**
     * @throws IllegalStateException if the calling thread _is_ on the main thread
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (currentIsOnMainThread()) {
            throw new IllegalStateException("must call _off_ the main thread");
        }
    }
}