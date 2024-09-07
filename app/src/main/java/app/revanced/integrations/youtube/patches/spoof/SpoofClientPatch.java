package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.chromium.net.UrlRequest;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.patches.spoof.requests.StreamingDataRequester;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT = Settings.SPOOF_CLIENT.get();

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    private static volatile Future<ByteBuffer> currentVideoStream;

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (SPOOF_CLIENT) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking 'get_watch' by returning unreachable uri");

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
     * <p>
     * Blocks /initplayback requests.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (SPOOF_CLIENT) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    Logger.printDebug(() -> "Blocking 'initplayback' by returning unreachable url");

                    return UNREACHABLE_HOST_URI_STRING;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockInitPlaybackRequest failure", ex);
            }
        }

        return originalUrlString;
    }

    /**
     * Injection point.
     */
    public static boolean isSpoofingEnabled() {
        return SPOOF_CLIENT;
    }

    /**
     * Injection point.
     */
    public static UrlRequest buildRequest(UrlRequest.Builder builder, String url,
                                          Map<String, String> playerHeaders) {
        if (SPOOF_CLIENT) {
            try {
                Uri uri = Uri.parse(url);
                String path = uri.getPath();
                if (path != null && path.contains("player") && !path.contains("heartbeat")) {
                    String videoId = Objects.requireNonNull(uri.getQueryParameter("id"));
                    currentVideoStream = StreamingDataRequester.fetch(videoId, playerHeaders);
                }
            } catch (Exception ex) {
                Logger.printException(() -> "buildRequest failure", ex);
            }
        }

        return builder.build();
    }

    /**
     * Injection point.
     * Fix playback by replace the streaming data.
     * Called after {@link #buildRequest(UrlRequest.Builder, String, Map)}.
     */
    @Nullable
    public static ByteBuffer getStreamingData(String videoId) {
        if (SPOOF_CLIENT) {
            try {
                // FIXME: Sometimes this is called on the main thread (such as when a Shorts advertisement loads)
                // But this method should be called off the main thread since it blocks on a network request.
                // Utils.verifyOffMainThread(); // TODO: figure out what to change to enable this.

                var future = currentVideoStream;
                if (future != null) {
                    final long maxSecondsToWait = 20;
                    var stream = future.get(maxSecondsToWait, TimeUnit.SECONDS);
                    if (stream != null) {
                        Logger.printDebug(() -> "Overriding video stream");
                        return stream;
                    }

                    Logger.printDebug(() -> "Not overriding streaming data (video stream is null)");
                }
            } catch (TimeoutException ex) {
                Logger.printInfo(() -> "getStreamingData timed out", ex);
            } catch (InterruptedException ex) {
                Logger.printException(() -> "getStreamingData interrupted", ex);
                Thread.currentThread().interrupt(); // Restore interrupt status flag.
            } catch (ExecutionException ex) {
                Logger.printException(() -> "getStreamingData failure", ex);
            }
        }

        return null;
    }

    /**
     * Injection point.
     * Called after {@link #getStreamingData(String)}.
     */
    @Nullable
    public static byte[] removeVideoPlaybackPostBody(Uri uri, int method, byte[] postData) {
        if (SPOOF_CLIENT) {
            try {
                final int methodPost = 2;
                if (method == methodPost) {
                    String path = uri.getPath();
                    String clientName = "c";
                    final boolean iosClient = ClientType.IOS.name().equals(uri.getQueryParameter(clientName));
                    if (iosClient && path != null && path.contains("videoplayback")) {
                        return null;
                    }
                }
            }  catch (Exception ex) {
                Logger.printException(() -> "removeVideoPlaybackPostBody failure", ex);
            }
        }

        return postData;
    }
}
