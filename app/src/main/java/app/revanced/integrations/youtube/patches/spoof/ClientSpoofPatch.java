package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static app.revanced.integrations.youtube.patches.spoof.requests.StoryboardRendererRequester.getStoryboardRenderer;

public class ClientSpoofPatch {
    private static final boolean CLIENT_SPOOF_ENABLED = Settings.CLIENT_SPOOF.get();
    private static final boolean CLIENT_SPOOF_USE_IOS = Settings.CLIENT_SPOOF_USE_IOS.get();

    private static final ClientType CLIENT_TYPE = CLIENT_SPOOF_USE_IOS ? ClientType.IOS : ClientType.ANDROID_TESTSUITE;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URL = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URL);

    /**
     * Parameters used when playing clips.
     */
    private static final String CLIPS_PARAMETERS = "kAIB";

    /**
     * Last video id loaded. Used to prevent reloading the same spec multiple times.
     */
    @Nullable
    private static volatile String lastPlayerResponseVideoId;

    @Nullable
    private static volatile Future<StoryboardRenderer> rendererFuture;

    private static volatile boolean isPlayingShorts;

    /**
     * Injection point.
     * Blocks /get_watch requests by returning a localhost URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return Localhost URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        try {
            if (isClientSpoofingEnabled()) {
                String path = playerRequestUri.getPath();
                if (path != null && path.contains("get_watch")) {
                    return UNREACHABLE_HOST_URI;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "blockGetWatchRequest failure", ex);
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * Blocks /initplayback requests.
     * For iOS, an unreachable host URL can be used, but for Android Testsuite, this is not possible.
     */
    public static String blockInitPlaybackRequest(String originalUrl) {
        if (isClientSpoofingEnabled()) {
            if (CLIENT_SPOOF_USE_IOS) {
                return UNREACHABLE_HOST_URL;
            } else {
                // TODO: Ideally, a local proxy could be setup and block the request the same way as Burp Suite is capable of
                //  because that way the request is never sent to YouTube unnecessarily.
                //  Just using localhost does unfortunately not work.
                return Uri.parse(originalUrl).buildUpon().clearQuery().build().toString();
            }
        }

        return originalUrl;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        if (isClientSpoofingEnabled()) {
            return CLIENT_TYPE.id;
        }

        return originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        if (isClientSpoofingEnabled()) {
            return CLIENT_TYPE.version;
        }

        return originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return CLIENT_SPOOF_ENABLED;
    }

    /**
     * Injection point.
     * <p>
     * Called off the main thread, and called multiple times for each video.
     *
     * @param parameters Original protobuf parameter value.
     */
    public static String hookParameter(String parameters, boolean isShortAndOpeningOrPlaying) {
        try {
            if (parameters == null || !CLIENT_SPOOF_ENABLED || CLIENT_SPOOF_USE_IOS) {
                return parameters;
            }

            // Clip's player parameters contain a lot of information (e.g. video start and end time or whether it loops)
            // For this reason, the player parameters of a clip are usually very long (150~300 characters).
            // Clips are 60 seconds or less in length, so no spoofing.
            if (parameters.startsWith(CLIPS_PARAMETERS)) {
                return parameters;
            }

            isPlayingShorts = VideoInformation.playerParametersAreShort(parameters);

            fetchStoryboardRenderer();
        } catch (Exception ex) {
            Logger.printException(() -> "spoofParameter failure", ex);
        }
        return parameters;
    }

    @Nullable
    private static StoryboardRenderer getRenderer(boolean waitForCompletion) {
        Future<StoryboardRenderer> future = rendererFuture;
        if (future != null) {
            try {
                if (waitForCompletion || future.isDone()) {
                    return future.get(20000, TimeUnit.MILLISECONDS); // Any arbitrarily large timeout.
                } // else, return null.
            } catch (TimeoutException ex) {
                Logger.printDebug(() -> "Could not get renderer (get timed out)");
            } catch (ExecutionException | InterruptedException ex) {
                // Should never happen.
                Logger.printException(() -> "Could not get renderer", ex);
            }
        }
        return null;
    }

    private static void fetchStoryboardRenderer() {
        if (!Settings.CLIENT_SPOOF_SPOOF_STORYBOARD_RENDERER.get()) {
            lastPlayerResponseVideoId = null;
            rendererFuture = null;
            return;
        }
        String videoId = VideoInformation.getPlayerResponseVideoId();
        if (!videoId.equals(lastPlayerResponseVideoId)) {
            rendererFuture = Utils.submitOnBackgroundThread(() -> getStoryboardRenderer(videoId));
            lastPlayerResponseVideoId = videoId;
        }
        // Block until the renderer fetch completes.
        // This is desired because if this returns without finishing the fetch
        // then video will start playback but the storyboard is not ready yet.
        getRenderer(true);
    }

    private static String getStoryboardRendererSpec(String originalStoryboardRendererSpec,
                                                    boolean returnNullIfLiveStream) {
        if (CLIENT_SPOOF_ENABLED && !CLIENT_SPOOF_USE_IOS) {
            StoryboardRenderer renderer = getRenderer(false);
            if (renderer != null) {
                if (returnNullIfLiveStream && renderer.isLiveStream()) {
                    return null;
                }
                String spec = renderer.getSpec();
                if (spec != null) {
                    return spec;
                }
            }
        }

        return originalStoryboardRendererSpec;
    }

    /**
     * Injection point.
     * Called from background threads and from the main thread.
     */
    @Nullable
    public static String getStoryboardRendererSpec(String originalStoryboardRendererSpec) {
        return getStoryboardRendererSpec(originalStoryboardRendererSpec, false);
    }

    /**
     * Injection point.
     * Uses additional check to handle live streams.
     * Called from background threads and from the main thread.
     */
    @Nullable
    public static String getStoryboardDecoderRendererSpec(String originalStoryboardRendererSpec) {
        return getStoryboardRendererSpec(originalStoryboardRendererSpec, true);
    }

    /**
     * Injection point.
     */
    public static int getRecommendedLevel(int originalLevel) {
        if (CLIENT_SPOOF_ENABLED && !CLIENT_SPOOF_USE_IOS) {
            StoryboardRenderer renderer = getRenderer(false);
            if (renderer != null) {
                Integer recommendedLevel = renderer.getRecommendedLevel();
                if (recommendedLevel != null) return recommendedLevel;
            }
        }

        return originalLevel;
    }

    /**
     * Injection point.  Forces seekbar to be shown for paid videos or
     * if {@link Settings#CLIENT_SPOOF_USE_IOS} is not enabled.
     */
    public static boolean getSeekbarThumbnailOverrideValue() {
        if (!CLIENT_SPOOF_ENABLED || CLIENT_SPOOF_USE_IOS) {
            return false;
        }
        StoryboardRenderer renderer = getRenderer(false);
        if (renderer == null) {
            // Spoof storyboard renderer is turned off,
            // video is paid, or the storyboard fetch timed out.
            // Show empty thumbnails so the seek time and chapters still show up.
            return true;
        }
        return renderer.getSpec() != null;
    }

    /**
     * Injection point.
     *
     * @param view seekbar thumbnail view.  Includes both shorts and regular videos.
     */
    public static void seekbarImageViewCreated(ImageView view) {
        try {
            if (!CLIENT_SPOOF_ENABLED || CLIENT_SPOOF_USE_IOS || Settings.CLIENT_SPOOF_SPOOF_STORYBOARD_RENDERER.get()) {
                return;
            }
            if (isPlayingShorts) return;

            view.setVisibility(View.GONE);
            // Also hide the border around the thumbnail (otherwise a 1 pixel wide bordered frame is visible).
            ViewGroup parentLayout = (ViewGroup) view.getParent();
            parentLayout.setPadding(0, 0, 0, 0);
        } catch (Exception ex) {
            Logger.printException(() -> "seekbarImageViewCreated failure", ex);
        }
    }

    enum ClientType {
        ANDROID_TESTSUITE(30, "1.9"),
        IOS(5, Utils.getAppVersionName());

        final int id;
        final String version;

        ClientType(int id, String version) {
            this.id = id;
            this.version = version;
        }
    }
}
