package app.revanced.integrations.patches;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Hooking class for the current playing video.
 */
public final class VideoInformation {
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerController;
    private static Method seekMethod;

    private static String videoId = "";
    private static long videoLength = 1;
    private static volatile long videoTime = -1; // must be volatile. Value is set off main thread from high precision patch hook

    /**
     * Hook into PlayerController.onCreate() method.
     *
     * @param thisRef Reference to the player controller object.
     */
    public static void playerController_onCreateHook(final Object thisRef) {
        playerController = new WeakReference<>(thisRef);
        videoLength = 1;
        videoTime = -1;

        try {
            seekMethod = thisRef.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            LogHelper.printException(() -> "Failed to initialize", ex);
        }
    }

    /**
     * Injection point.
     *
     * @param videoId The id of the current video.
     */
    public static void setVideoId(String videoId) {
        LogHelper.printDebug(() -> "Current video id: " + videoId);

        VideoInformation.videoId = videoId;
    }

    /**
     * Injection point.
     *
     * @param length The length of the video in milliseconds.
     */
    public static void setVideoLength(final long length) {
        if (videoLength != length) {
            LogHelper.printDebug(() -> "Current video length: " + length);
            videoLength = length;
        }
    }

    /**
     * Injection point.
     *
     * @param time The playback time of the video in milliseconds.
     */
    public static void setVideoTime(final long time) {
        if (videoTime != time) {
            LogHelper.printDebug(() -> "Current video time: " + time);
            videoTime = time;
        }
    }

    /**
     * Seek on the current video.
     * <b>Currently this does not function for Shorts playback.</b>
     *
     * @param millisecond The millisecond to seek the video to.
     * @return if the seek was successful
     */
    public static boolean seekTo(final long millisecond) {
        ReVancedUtils.verifyOnMainThread();
        if (seekMethod == null) {
            LogHelper.printException(() -> "seekMethod was null");
            return false;
        }

        try {
            LogHelper.printDebug(() -> "Seeking to " + millisecond);
            return (Boolean) seekMethod.invoke(playerController.get(), millisecond);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to seek", ex);
            return false;
        }
    }

    public static boolean seekToRelative(long millisecondsRelative) {
        return seekTo(videoTime + millisecondsRelative);
    }

    /**
     * Id of the current video playing.
     * <b>Currently this does not function for Shorts playback.</b>
     *
     * @return The id of the video. Empty string if not set yet.
     */
    public static String getCurrentVideoId() {
        return videoId;
    }

    /**
     * Length of the current video playing.
     * Includes Shorts playback.
     *
     * @return The length of the video in milliseconds. 1 if not set yet.
     */
    public static long getCurrentVideoLength() {
       return videoLength;
    }

    /**
     * Playback time of the current video playing.
     * Value can lag up to approximately 100ms behind the actual current video playback time.
     *
     * Note: Code inside a videoTimeHook patch callback
     * should use the callback video time and avoid using this method
     * (in rare recursive situations this method may not yet be updated).
     *
     * Includes Shorts playback.
     *
     * @return The time of the video in milliseconds. -1 if not set yet.
     */
    public static long getVideoTime() {
        return videoTime;
    }

    /**
     * @return If the playback is at the end of the video
     */
    public static boolean isAtEndOfVideo() {
        return videoTime > 0 && videoTime >= videoLength;
    }

}
