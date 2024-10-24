package app.revanced.integrations.shared.react;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
public abstract class BaseRemoteReactPreloadScriptBootstrapper extends BaseReactPreloadScriptBootstrapper {
    protected final void download(String url, File preloadScriptFile, int bufferSize) {
        final var eTagFile = getWorkingDirectoryFile(preloadScriptFile.getName() + ".etag");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            if (eTagFile.exists() && preloadScriptFile.exists()) {
                connection.setRequestProperty("If-None-Match", read(eTagFile, 256));
            }
            connection.connect();

            if (connection.getResponseCode() == 304) {
                connection.disconnect();
                return;
            }

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed to download the preload script: " + connection.getResponseCode());
            }

            final var eTagHeader = connection.getHeaderField("ETag");
            if (eTagHeader != null) {
                write(new ByteArrayInputStream(eTagHeader.getBytes()), eTagFile, 256);
            }

            write(connection.getInputStream(), preloadScriptFile, bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
