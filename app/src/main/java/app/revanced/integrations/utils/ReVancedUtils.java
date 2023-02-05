package app.revanced.integrations.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.text.Bidi;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReVancedUtils {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private ReVancedUtils() {
    } // utility class

    /**
     * Maximum number of background threads run concurrently
     */
    private static final int SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS = 20;

    /**
     * General purpose pool for network calls and other background tasks.
     * All tasks run at max thread priority.
     */
    private static final ThreadPoolExecutor backgroundThreadPool = new ThreadPoolExecutor(
            2, // minimum 2 threads always ready to be used
            10, // For any threads over the minimum, keep them alive 10 seconds after they go idle
            SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setPriority(Thread.MAX_PRIORITY); // run at max priority
                    return t;
                }
            });

    private static void checkIfPoolHasReachedLimit() {
        if (backgroundThreadPool.getActiveCount() >= SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS) {
            // Something is wrong. Background threads are piling up and not completing as expected,
            // or some ReVanced code is submitting an unexpected number of background tasks.
            LogHelper.printException(() -> "Reached maximum background thread count of "
                    + SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS + " threads");
        }
    }

    public static void runOnBackgroundThread(Runnable task) {
        backgroundThreadPool.execute(task);
        checkIfPoolHasReachedLimit();
    }

    public static <T> Future<T> submitOnBackgroundThread(Callable<T> call) {
        Future<T> future = backgroundThreadPool.submit(call);
        checkIfPoolHasReachedLimit();
        return future;
    }

    public static boolean containsAny(final String value, final String... targets) {
        for (String string : targets)
            if (!string.isEmpty() && value.contains(string)) return true;
        return false;
    }

    public static int getResourceIdByName(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    public static int getIdentifier(String name, String defType) {
        return getResourceIdByName(getContext(), name, defType);
    }

    public static int getResourceInteger(String resourceIdentifierName) {
        return getContext().getResources().getInteger(getIdentifier(resourceIdentifierName, "integer"));
    }

    public static Animation getResourceAnimation(String resourceIdentifierName) {
        return AnimationUtils.loadAnimation(getContext(), getIdentifier(resourceIdentifierName, "anim"));
    }

    public static Context getContext() {
        if (context != null) {
            return context;
        } else {
            LogHelper.printException(() -> "Context is null, returning null!");
            return null;
        }
    }

    public static void setClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("ReVanced", text);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    private static Boolean isRightToLeftTextLayout;
    /**
     * If the device language uses right to left text layout (hebrew, arabic, etc)
     */
    public static boolean isRightToLeftTextLayout() {
        if (isRightToLeftTextLayout == null) {
            String displayLanguage = Locale.getDefault().getDisplayLanguage();
            isRightToLeftTextLayout = new Bidi(displayLanguage, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft();
        }
        return isRightToLeftTextLayout;
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastShort(String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_SHORT);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastLong(String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_LONG);
    }

    private static void showToast(String messageToToast, int toastDuration) {
        Objects.requireNonNull(messageToToast);
        runOnMainThreadNowOrLater(() -> {
                    // cannot use getContext(), otherwise if context is null it will cause infinite recursion of error logging
                    if (context == null) {
                        LogHelper.printDebug(() -> "Cannot show toast (context is null)");
                    } else {
                        LogHelper.printDebug(() -> "Showing toast: " + messageToToast);
                        Toast.makeText(context, messageToToast, toastDuration).show();
                    }
                }
        );
    }

    /**
     * Automatically logs any exceptions the runnable throws.
     *
     * @see #runOnMainThreadNowOrLater(Runnable)
     */
    public static void runOnMainThread(Runnable runnable) {
        runOnMainThreadDelayed(runnable, 0);
    }

    /**
     * Automatically logs any exceptions the runnable throws
     */
    public static void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        Runnable loggingRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                LogHelper.printException(() -> runnable.getClass() + ": " + ex.getMessage(), ex);
            }
        };
        new Handler(Looper.getMainLooper()).postDelayed(loggingRunnable, delayMillis);
    }

    /**
     * If called from the main thread, the code is run immediately.<p>
     * If called off the main thread, this is the same as {@link #runOnMainThread(Runnable)}.
     */
    public static void runOnMainThreadNowOrLater(Runnable runnable) {
        if (currentlyIsOnMainThread()) {
            runnable.run();
        } else {
            runOnMainThread(runnable);
        }
    }

    /**
     * @return if the calling thread is on the main thread
     */
    public static boolean currentlyIsOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }

    /**
     * @throws IllegalStateException if the calling thread is _off_ the main thread
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (!currentlyIsOnMainThread()) {
            throw new IllegalStateException("Must call _on_ the main thread");
        }
    }

    /**
     * @throws IllegalStateException if the calling thread is _on_ the main thread
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (currentlyIsOnMainThread()) {
            throw new IllegalStateException("Must call _off_ the main thread");
        }
    }

    /**
     * Useful to check if user is watching offline downloaded videos.
     *
     * @return if connected to a network
     */
    @SuppressLint("MissingPermission") // permissions already included in YouTube
    public static boolean isNetworkConnected() {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}