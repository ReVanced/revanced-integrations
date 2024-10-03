package app.revanced.integrations.youtube.patches.spoof.requests;

import static app.revanced.integrations.youtube.patches.spoof.requests.PlayerRoutes.GET_STREAMING_DATA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.patches.spoof.ClientType;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * Video streaming data.  Fetching is tied to the behavior YT uses,
 * where this class fetches the streams only when YT fetches.
 *
 * Effectively the cache expiration of these fetches is the same as the stock app,
 * since the stock app would not use expired streams and therefor
 * the integrations replace stream hook is called only if YT
 * would have used it's own client streams.
 */
public class StreamingDataRequest {

    private static final ClientType[] CLIENT_ORDER_TO_USE;

    static {
        ClientType[] allClientTypes = ClientType.values();
        ClientType preferredClient = Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get();

        CLIENT_ORDER_TO_USE = new ClientType[allClientTypes.length];
        CLIENT_ORDER_TO_USE[0] = preferredClient;

        int i = 1;
        for (ClientType c : allClientTypes) {
            if (c != preferredClient) {
                CLIENT_ORDER_TO_USE[i++] = c;
            }
        }
    }

    private static final String[] REQUEST_HEADER_KEYS = {
            "Authorization", // Available only to logged in users.
            "X-GOOG-API-FORMAT-VERSION",
            "X-Goog-Visitor-Id"
    };

    /**
     * TCP connection and HTTP read timeout.
     */
    private static final int HTTP_TIMEOUT_MILLISECONDS = 10 * 1000;

    /**
     * Any arbitrarily large value, but must be at least twice {@link #HTTP_TIMEOUT_MILLISECONDS}
     */
    private static final int MAX_MILLISECONDS_TO_WAIT_FOR_FETCH = 20 * 1000;

    private static final Map<String, StreamingDataRequest> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(100) {
                /**
                 * Cache limit must be greater than the maximum number of videos open at once,
                 * which theoretically is more than 4 (3 Shorts + one regular minimized video).
                 * But instead use a much larger value, to handle if a video viewed a while ago
                 * is somehow still referenced.  Each stream is a small array of Strings
                 * so memory usage is not a concern.
                 */
                private static final int CACHE_LIMIT = 50;

                @Override
                protected boolean removeEldestEntry(Entry eldest) {
                    return size() > CACHE_LIMIT; // Evict the oldest entry if over the cache limit.
                }
            });

    public static void fetchRequest(String videoId, Map<String, String> fetchHeaders) {
        // Always fetch, even if there is a existing request for the same video.
        cache.put(videoId, new StreamingDataRequest(videoId, fetchHeaders));
    }

    @Nullable
    public static StreamingDataRequest getRequestForVideoId(String videoId) {
        return cache.get(videoId);
    }

    private static void handleConnectionError(String toastMessage, @Nullable Exception ex, boolean showToast) {
        if (showToast) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    @Nullable
    private static HttpURLConnection send(ClientType clientType, String videoId,
                                          Map<String, String> playerHeaders,
                                          boolean showErrorToasts) {
        Objects.requireNonNull(clientType);
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(playerHeaders);

        final long startTime = System.currentTimeMillis();
        String clientTypeName = clientType.name();
        Logger.printDebug(() -> "Fetching video streams for: " + videoId + " using client: " + clientType.name());

        try {
            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STREAMING_DATA, clientType);
            connection.setConnectTimeout(HTTP_TIMEOUT_MILLISECONDS);
            connection.setReadTimeout(HTTP_TIMEOUT_MILLISECONDS);

            for (String key : REQUEST_HEADER_KEYS) {
                String value = playerHeaders.get(key);
                if (value != null) {
                    connection.setRequestProperty(key, value);
                }
            }

            String innerTubeBody = String.format(PlayerRoutes.createInnertubeBody(clientType), videoId);
            byte[] requestBody = innerTubeBody.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(requestBody.length);
            connection.getOutputStream().write(requestBody);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;

            handleConnectionError(clientTypeName + " not available with response code: "
                            + responseCode + " message: " + connection.getResponseMessage(),
                    null, showErrorToasts);
        } catch (SocketTimeoutException ex) {
            handleConnectionError("Connection timeout", ex, showErrorToasts);
        } catch (IOException ex) {
            handleConnectionError("Network error", ex, showErrorToasts);
        } catch (Exception ex) {
            Logger.printException(() -> "send failed", ex);
        } finally {
            Logger.printDebug(() -> "video: " + videoId + " took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    private static ByteBuffer fetch(String videoId, Map<String, String> playerHeaders) {
        final boolean debugEnabled = BaseSettings.DEBUG.get();

        // Retry with different client if empty response body is received.
        int i = 0;
        for (ClientType clientType : CLIENT_ORDER_TO_USE) {
            // Show an error if the last client type fails, or if the debug is enabled then show for all attempts.
            final boolean showErrorToast = (++i == CLIENT_ORDER_TO_USE.length) || debugEnabled;

            HttpURLConnection connection = send(clientType, videoId, playerHeaders, showErrorToast);
            if (connection != null) {
                try {
                    // gzip encoding doesn't response with content length (-1),
                    // but empty response body does.
                    if (connection.getContentLength() != 0) {
                        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                byte[] buffer = new byte[2048];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                                    baos.write(buffer, 0, bytesRead);
                                }

                                return ByteBuffer.wrap(baos.toByteArray());
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.printException(() -> "Fetch failed while processing response data", ex);
                }
            }
        }

        handleConnectionError("Could not fetch any client streams", null, debugEnabled);
        return null;
    }

    private final String videoId;
    private final Future<ByteBuffer> future;

    private StreamingDataRequest(String videoId, Map<String, String> playerHeaders) {
        Objects.requireNonNull(playerHeaders);
        this.videoId = videoId;
        this.future = Utils.submitOnBackgroundThread(() -> fetch(videoId, playerHeaders));
    }

    public boolean fetchCompleted() {
        return future.isDone();
    }

    @Nullable
    public ByteBuffer getStream() {
        try {
            return future.get(MAX_MILLISECONDS_TO_WAIT_FOR_FETCH, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Logger.printInfo(() -> "getStream timed out", ex);
        } catch (InterruptedException ex) {
            Logger.printException(() -> "getStream interrupted", ex);
            Thread.currentThread().interrupt(); // Restore interrupt status flag.
        } catch (ExecutionException ex) {
            Logger.printException(() -> "getStream failure", ex);
        }

        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "StreamingDataRequest{" + "videoId='" + videoId + '\'' + '}';
    }
}
