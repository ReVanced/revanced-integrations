package app.revanced.integrations.patches;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public final class DeArrowPatch {

    private static final boolean DEARROW_ENABLED;
    private static final String ORIGINAL_URL_PARAMETER = "&originalUrl=";
    private static final String LOCAL_HOST_URL_PREFIX;
    private static final int thumbnailServerPort;
    private static final ServerSocket socket;

    static {
        boolean enabled = SettingsEnum.DEARROW_ENABLED.getBoolean();
        ServerSocket serverSocket = null;
        int port = -1;
        if (enabled) {
            try {
                // Fix any bad imported data.
                final int secondaryImageType = SettingsEnum.DEARROW_ALTERNATE_IMAGE_TYPE.getInt();
                if (secondaryImageType < 0 || secondaryImageType > 3) {
                    LogHelper.printException(() -> "Invalid DeArrow alternate thumbnail image type: " + secondaryImageType);
                    SettingsEnum.DEARROW_ALTERNATE_IMAGE_TYPE.saveValue(SettingsEnum.DEARROW_ALTERNATE_IMAGE_TYPE.defaultValue);
                }
                serverSocket = new ServerSocket();
                port = serverSocket.getLocalPort();
            } catch (Exception ex) {
                LogHelper.printException(() -> "Failed to start thumbnail server", ex);
                enabled = false;
            }
        }

        socket = serverSocket;
        thumbnailServerPort = port;
        // FIXME: requires manifest to change to allow non https (and probably also needs Cronet patch to allow non http)
        LOCAL_HOST_URL_PREFIX = "http://localhost:" + port + "/";
        DEARROW_ENABLED = enabled;
        if (enabled) {
            LogHelper.printDebug(() -> "Thumbnail server listening on port " + thumbnailServerPort);
            new Thread(() -> handleThumbnailRequest()).start();
        }
    }

    private static void handleThumbnailRequest() {
        while (true) {
            try (Socket clientSocket = socket.accept()) {
                LogHelper.printDebug(() -> "getRemoteSocketAddress: " + clientSocket.getRemoteSocketAddress());
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String requestLine = in.readLine(); // Read the first line of the request

                    if (requestLine != null && requestLine.startsWith("GET")) {
                        String requestedUrl = requestLine.split(" ")[1];
                        requestedUrl = requestedUrl.substring(1); // remove forward slash
                        final String requestedUrlLog = requestedUrl;
                        LogHelper.printDebug(() -> "Handling request: " + requestedUrlLog);
//                        if (!requestedUrl.startsWith("https://")) {
//                            LogHelper.printDebug(() -> "Ignoring request: " + requestedUrlLog);
//                            continue;
//                        }
                        final int originalUrlIndex = requestedUrl.indexOf(ORIGINAL_URL_PARAMETER);
                        if (originalUrlIndex < 0) {
                            LogHelper.printException(() -> "Original url is missing: " + requestedUrlLog); // Should never happen.
                            continue;
                        }
                        String firstAttemptUrl = requestedUrl.substring(0, originalUrlIndex);
                        if (!streamUrlToClient(clientSocket, firstAttemptUrl)) {
                            LogHelper.printDebug(() -> "First attempt url failed: " + firstAttemptUrl);
                            String originalUrl = requestedUrl.substring(originalUrlIndex + ORIGINAL_URL_PARAMETER.length());
                            if (!streamUrlToClient(clientSocket, originalUrl)) {
                                LogHelper.printDebug(() -> "Original url failed: " + originalUrl);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LogHelper.printException(() -> "Thumbnail server failure", ex);
            }
        }
    }

    private static boolean streamUrlToClient(Socket clientSocket, String url) {
        try {
            HttpURLConnection thumbnailConnection = (HttpURLConnection) new URL(url).openConnection();
            thumbnailConnection.setRequestMethod("GET");
            if (thumbnailConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }

            OutputStream clientOutputStream = clientSocket.getOutputStream();
            try (PrintWriter out = new PrintWriter(clientOutputStream)) {
                // Pass through external server's response headers to the client.
                out.println("HTTP/1.1 200 OK");
                for (int i = 0; ; i++) {
                    String key = thumbnailConnection.getHeaderFieldKey(i);
                    String header = thumbnailConnection.getHeaderField(i);
                    if (key == null || header == null) {
                        break;
                    }
                    out.println(key + ": " + header);
                }
                out.println(); // Empty line to indicate the end of headers.
                out.flush();

                // Pass through the image data.
                InputStream imageStream = thumbnailConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = imageStream.read(buffer)) != -1) {
                    clientOutputStream.write(buffer, 0, bytesRead);
                }
                clientOutputStream.flush();
                return true;
            }
        } catch (IOException ex) {
            LogHelper.printInfo(() -> "url fetch failed:" + url, ex);
            return false;
        }
    }


    public static String overrideImageURL(String originalUrl) {
        try {
            if (!DEARROW_ENABLED) return originalUrl;
            LogHelper.printDebug(() -> "Image url: " + originalUrl);

            String thumbnailPrefix = "https://i.ytimg.com/vi"; // '/vi/' or '/vi_webp/'
            if (!originalUrl.startsWith(thumbnailPrefix)) return originalUrl;

            final int videoIdStartIndex = originalUrl.indexOf('/', thumbnailPrefix.length()) + 1;
            if (videoIdStartIndex <= 0) return originalUrl;
            final int videoIdEndIndex = originalUrl.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return originalUrl;
            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = originalUrl.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return originalUrl;

            String originalImageSize = originalUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            final String alternateImagePrefix;
            switch (originalImageSize) {
                case "maxresdefault":
                    // No in video thumbnails for this size.  Fall thru to next largest.
                    // Of note, the YouTube app/website does not seem to ever use the max res size.
                case "hq720":
                    alternateImagePrefix = "hq720_";
                    break;
                case "sddefault":
                    alternateImagePrefix = "sd";
                    break;
                case "hqdefault":
                    alternateImagePrefix = "hq";
                    break;
                case "mqdefault":
                    alternateImagePrefix = "mq";
                    break;
                default:
                    return originalUrl; // Thumbnail is a short or some unknown image type.
            }
            final String alternateImageName;
            final int alternateThumbnailType = SettingsEnum.DEARROW_ALTERNATE_IMAGE_TYPE.getInt();
            if (alternateThumbnailType == 0) {
                alternateImageName = originalImageSize;
            } else {
                alternateImageName = alternateImagePrefix + alternateThumbnailType;
            }

            String videoId = originalUrl.substring(videoIdStartIndex, videoIdEndIndex);
            String replacement = LOCAL_HOST_URL_PREFIX
                    + "https://dearrow-thumb.ajay.app/api/v1/getThumbnail" + "?videoID=" + videoId
                    + "&redirectUrl=" + "https://i.ytimg.com/vi_webp/" + videoId + "/" + alternateImageName + ".webp"
                    + ORIGINAL_URL_PARAMETER + originalUrl;
            LogHelper.printDebug(() -> "Replaced image with: " + replacement);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "DeArrow failure", ex);
            return originalUrl;
        }
    }

    public static void handleCronetFailure(Object request, Object responseInfo, IOException exception) {
        LogHelper.printDebug(() -> "handleCronetFailure request: " + request + " response: " + responseInfo + " exception:" + exception);
    }
}
