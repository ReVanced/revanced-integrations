package app.revanced.integrations.patches;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Hooking class for the current playing video.
 */
public final class VideoInformation {
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerController;
    private static Method seekMethod;

    private static String videoId = "";
    private static long videoLength = 1;
    private static long videoTime = -1;


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
     * Set the video id.
     *
     * @param videoId The id of the video.
     */
    public static void setVideoId(String videoId) {
        LogHelper.printDebug(() -> "Current video id: " + videoId);

        VideoInformation.videoId = videoId;
    }

    /**
     * Set the video length.
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
     * Set the video time.
     *
     * @param time The time of the video in milliseconds.
     */
    public static void setVideoTime(final long time) {
        if (videoTime != time) {
            LogHelper.printDebug(() -> "Current video time: " + time);
            videoTime = time;
        }
    }

    /**
     * Seek on the current video.
     * Currently this does not function for Shorts playback.
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
     * Get the id of the current video playing.
     * Currently this does not function for Shorts playback.
     *
     * @return The id of the video. Empty string if not set yet.
     */
    public static String getCurrentVideoId() {
        return videoId;
    }

    /**
     * Get the length of the current video playing.
     * Currently this does not function for Shorts playback.
     *
     * @return The length of the video in milliseconds. 1 if not set yet.
     */
    public static long getCurrentVideoLength() {
       return videoLength;
    }

    /**
     * Get the time of the current video playing.
     * Currently this does not function for Shorts playback.
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
