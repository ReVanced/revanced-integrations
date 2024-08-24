package app.revanced.integrations.youtube.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.annotation.SuppressLint;

import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static app.revanced.integrations.youtube.patches.spoof.requests.PlayerRoutes.*;

public class StreamingDataRequester {
    private static final boolean showToastOnException = false;

    private StreamingDataRequester() {
    }

    private static void handleConnectionError(String toastMessage, @Nullable Exception ex, boolean showToast) {
        if (showToast) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    @Nullable
    private static HttpURLConnection send(String requestBody, String authHeader) {
        final long startTime = System.currentTimeMillis();
        try {
            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STREAMING_DATA);

            // Required for age restricted videos.
            if (authHeader != null) {
                connection.setRequestProperty("authorization", authHeader);
            }

            final byte[] innerTubeBody = requestBody.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(innerTubeBody.length);
            connection.getOutputStream().write(innerTubeBody);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;

            handleConnectionError("Not available: " + responseCode, null,
                showToastOnException || BaseSettings.DEBUG_TOAST_ON_ERROR.get());
        } catch (SocketTimeoutException ex) {
            handleConnectionError("Connection timeout.", ex, showToastOnException);
        } catch (IOException ex) {
            handleConnectionError("Network error.", ex, showToastOnException);
        } catch (Exception ex) {
            Logger.printException(() -> "Request failed.", ex);
        } finally {
            Logger.printDebug(() -> "Took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    @SuppressLint("NewApi")
    public static CompletableFuture<ByteBuffer> fetch(@NonNull String videoId, String authHeader) {
        Objects.requireNonNull(videoId);

        return CompletableFuture.supplyAsync(() -> {
            ByteBuffer finalBuffer = null;

            // Retry with different client if empty streaming data is received.
            List<String> innerTubeBodies = List.of(
                VR_INNER_TUBE_BODY,
                UNPLUGGED_INNER_TUBE_BODY,
                TESTSUITE_INNER_TUBE_BODY
            );

            for (String body : innerTubeBodies) {
                HttpURLConnection connection = send(String.format(body, videoId), authHeader);
                if (connection != null) {
                    try {
                        // gzip encoding doesn't response with content length (-1),
                        // but empty response body does.
                        if (connection.getContentLength() != 0) {
                            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                baos.write(buffer, 0, bytesRead);
                            }
                            finalBuffer = ByteBuffer.wrap(baos.toByteArray());
                            break;
                        }
                    } catch (IOException ex) {
                        Logger.printException(() -> "Failed while processing response data.", ex);
                    }
                }
            }

            if (finalBuffer == null) {
                handleConnectionError("No streaming data available.", null, showToastOnException);
            }

            return finalBuffer;
        });
    }
}
