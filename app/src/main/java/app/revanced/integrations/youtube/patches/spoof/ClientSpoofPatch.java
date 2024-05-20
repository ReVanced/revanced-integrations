package app.revanced.integrations.youtube.patches.spoof;

import static app.revanced.integrations.youtube.patches.spoof.requests.StoryboardRendererRequester.getStoryboardRenderer;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ClientSpoofPatch {
    private static final boolean CLIENT_SPOOF_ENABLED = Settings.CLIENT_SPOOF.get();
    private static final boolean CLIENT_SPOOF_USE_IOS = Settings.CLIENT_SPOOF_USE_IOS.get();
    private static final boolean CLIENT_SPOOF_STORYBOARD = CLIENT_SPOOF_ENABLED && !CLIENT_SPOOF_USE_IOS;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    @Nullable
    private static volatile Future<StoryboardRenderer> lastStoryboardFetched;

    private static final Map<String, Future<StoryboardRenderer>> storyboardCache =
            Collections.synchronizedMap(new LinkedHashMap<>(100) {
                private static final int CACHE_LIMIT = 100;

                @Override
                protected boolean removeEldestEntry(Entry eldest) {
                    return size() > CACHE_LIMIT; // Evict the oldest entry if over the cache limit.
                }
            });

    /**
     * Injection point.
     * Blocks /get_watch requests by returning a localhost URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return Localhost URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (CLIENT_SPOOF_ENABLED) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking: " + playerRequestUri + " by returning: " + UNREACHABLE_HOST_URI_STRING);

                    return UNREACHABLE_HOST_URI;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockGetWatchRequest failure", ex);
            }
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * Blocks /initplayback requests.
     * For iOS, an unreachable host URL can be used, but for Android Testsuite, this is not possible.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (CLIENT_SPOOF_ENABLED) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    String replacementUriString = (getSpoofClientType() == ClientType.IOS)
                            ? UNREACHABLE_HOST_URI_STRING
                            // TODO: Ideally, a local proxy could be setup and block
                            //  the request the same way as Burp Suite is capable of
                            //  because that way the request is never sent to YouTube unnecessarily.
                            //  Just using localhost unfortunately does not work.
                            : originalUri.buildUpon().clearQuery().build().toString();

                    Logger.printDebug(() -> "Blocking: " + originalUrlString + " by returning: " + replacementUriString);

                    return replacementUriString;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockInitPlaybackRequest failure", ex);
            }
        }

        return originalUrlString;
    }

    private static ClientType getSpoofClientType() {
        if (CLIENT_SPOOF_USE_IOS) {
            return ClientType.IOS;
        }

        // Video is private or otherwise not available.
        // Test client still works for video playback, but seekbar thumbnails are not available.
        // Use iOS client instead.
        StoryboardRenderer renderer = getRenderer(false);
        if (renderer == null) {
            Logger.printDebug(() -> "Using iOS client for paid or otherwise restricted video");
            return ClientType.IOS;
        }

        // Test client does not support live streams.
        // Use the storyboard renderer information to fallback to iOS if a live stream is opened.
        if (renderer.isLiveStream) {
            Logger.printDebug(() -> "Using iOS client for livestream: " + renderer.videoId);
            return ClientType.IOS;
        }

        return ClientType.ANDROID_TESTSUITE;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        if (CLIENT_SPOOF_ENABLED) {
            return getSpoofClientType().id;
        }

        return originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        if (CLIENT_SPOOF_ENABLED) {
            return getSpoofClientType().version;
        }

        return originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return CLIENT_SPOOF_ENABLED;
    }

    //
    // Storyboard.
    //

    /**
     * Injection point.
     */
    public static String setPlayerResponseVideoId(String parameters, String videoId, boolean isShortAndOpeningOrPlaying) {
        if (CLIENT_SPOOF_STORYBOARD) {
            try {
                // VideoInformation is not a dependent patch, and only this single helper method is used.
                // Hook can be called when scrolling thru the feed and a Shorts shelf is present.
                // Ignore these videos.
                if (!isShortAndOpeningOrPlaying && VideoInformation.playerParametersAreShort(parameters)) {
                    Logger.printDebug(() -> "Ignoring Short: " + videoId);
                    return parameters;
                }

                Future<StoryboardRenderer> storyboard = storyboardCache.get(videoId);
                if (storyboard == null) {
                    storyboard = Utils.submitOnBackgroundThread(() -> getStoryboardRenderer(videoId));
                    storyboardCache.put(videoId, storyboard);
                    lastStoryboardFetched = storyboard;

                    // Block until the renderer fetch completes.
                    // This is desired because if this returns without finishing the fetch
                    // then video will start playback but the storyboard is not ready yet.
                    getRenderer(true);
                } else {
                    lastStoryboardFetched = storyboard;
                    // No need to block on the fetch since it previously loaded.
                }

            } catch (Exception ex) {
                Logger.printException(() -> "setPlayerResponseVideoId failure", ex);
            }
        }

        return parameters; // Return the original value since we are observing and not modifying.
    }

    @Nullable
    private static StoryboardRenderer getRenderer(boolean waitForCompletion) {
        var future = lastStoryboardFetched;
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

    /**
     * Injection point.
     * Called from background threads and from the main thread.
     */
    @Nullable
    public static String getStoryboardRendererSpec(String originalStoryboardRendererSpec) {
        if (CLIENT_SPOOF_STORYBOARD) {
            StoryboardRenderer renderer = getRenderer(false);

            if (renderer != null) {
                if (!renderer.isLiveStream && renderer.spec != null) {
                    return renderer.spec;
                }
            }
        }

        return originalStoryboardRendererSpec;
    }

    /**
     * Injection point.
     */
    public static int getRecommendedLevel(int originalLevel) {
        if (CLIENT_SPOOF_STORYBOARD) {
            StoryboardRenderer renderer = getRenderer(false);

            if (renderer != null) {
                if (!renderer.isLiveStream && renderer.recommendedLevel != null) {
                    return renderer.recommendedLevel;
                }
            }
        }

        return originalLevel;
    }

    private enum ClientType {
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
