package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.chromium.net.UrlRequest;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.patches.spoof.requests.StreamingDataRequest;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT = Settings.SPOOF_CLIENT.get();

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

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
                    StreamingDataRequest.fetchRequest(videoId, playerHeaders);
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
                // This hook is always called off the main thread,
                // but this can later be called for the same video id from the main thread.
                // This is not a concern, since the fetch will always be finished
                // and never block the main thread.

                StreamingDataRequest request = StreamingDataRequest.getRequestForVideoId(videoId);
                if (request != null) {
                    var stream = request.getStream();
                    if (stream != null) {
                        Logger.printDebug(() -> "Overriding video stream: " + videoId);
                        return stream;
                    }
                }

                Logger.printDebug(() -> "Not overriding streaming data (video stream is null): "  + videoId);
            } catch (Exception ex) {
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
