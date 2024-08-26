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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.patches.spoof.ClientType;

public class StreamingDataRequester {

    private StreamingDataRequester() {
    }

    private static void handleConnectionError(String toastMessage, @Nullable Exception ex, boolean showToast) {
        if (showToast) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    @Nullable
    private static HttpURLConnection send(ClientType clientType, String videoId, Map<String, String> playerHeaders) {
        final long startTime = System.currentTimeMillis();
        String clientTypeName = clientType.name();
        // Only show toast for each attempt if debug mode is enabled,
        // as the calling code shows a toast if all stream calls fail.
        final boolean showErrorToasts = BaseSettings.DEBUG.get();
        Logger.printDebug(() -> "Fetching video streams using client: " + clientType.name());

        try {
            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STREAMING_DATA, clientType);

            String authHeader = playerHeaders.get("Authorization");
            String visitorId = playerHeaders.get("X-Goog-Visitor-Id");
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("X-Goog-Visitor-Id", visitorId);

            String innerTubeBody = String.format(PlayerRoutes.createInnertubeBody(clientType), videoId);
            byte[] requestBody = innerTubeBody.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(requestBody.length);
            connection.getOutputStream().write(requestBody);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;

            handleConnectionError(clientTypeName + " not available with response code: " + responseCode,
                    null, showErrorToasts);
        } catch (SocketTimeoutException ex) {
            handleConnectionError("Connection timeout", ex, showErrorToasts);
        } catch (IOException ex) {
            handleConnectionError("Network error", ex, showErrorToasts);
        } catch (Exception ex) {
            Logger.printException(() -> "send failed", ex);
        } finally {
            Logger.printDebug(() -> clientTypeName + " took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    public static Future<ByteBuffer> fetch(@NonNull String videoId, Map<String, String> playerHeaders) {
        Objects.requireNonNull(videoId);

        return Utils.submitOnBackgroundThread(() -> {
            // Retry with different client if empty response body is received.
            ClientType[] clientTypesToUse = {
                    ClientType.IOS,
                    ClientType.ANDROID_VR
            };

            for (ClientType clientType : clientTypesToUse) {
                HttpURLConnection connection = send(clientType, videoId, playerHeaders);
                if (connection != null) {
                    try {
                        // gzip encoding doesn't response with content length (-1),
                        // but empty response body does.
                        if (connection.getContentLength() != 0) {
                            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                                    baos.write(buffer, 0, bytesRead);
                                }
                                return ByteBuffer.wrap(baos.toByteArray());
                            }
                        }
                    } catch (IOException ex) {
                        Logger.printException(() -> "Fetch failed while processing response data", ex);
                    }
                }
            }

            handleConnectionError("Fetch client spoof streams failed", null, true);
            return null;
        });
    }
}
