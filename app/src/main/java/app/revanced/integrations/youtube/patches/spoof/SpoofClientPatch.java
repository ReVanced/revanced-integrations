package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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

    private static volatile Map<String, String> fetchHeaders;

    private static volatile String lastFetchedVideoId;

    private static final ConcurrentHashMap<String, Future<ByteBuffer>> streamingDataCache = new ConcurrentHashMap<>();

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
    public static void setFetchHeaders(String url, Map<String, String> headers) {
        if (SPOOF_CLIENT) {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (path != null && path.contains("browse")) {
                fetchHeaders = headers;
            }
        }
    }

    /**
     * Injection point.
     */
    public static void fetchStreamingData(@NonNull String videoId, boolean unused) {
        if (SPOOF_CLIENT) {
            if (videoId.equals(lastFetchedVideoId)) return;

            if (streamingDataCache.containsKey(videoId)) return;

	        Future<ByteBuffer> streamingData = StreamingDataRequester.fetch(videoId, fetchHeaders);
    	    streamingDataCache.put(videoId, streamingData);
	        lastFetchedVideoId = videoId;
	    }
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
                Utils.verifyOffMainThread();

                var future = streamingDataCache.get(videoId);
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
