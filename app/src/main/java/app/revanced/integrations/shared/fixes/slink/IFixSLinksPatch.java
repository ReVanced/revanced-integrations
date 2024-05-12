package app.revanced.integrations.shared.fixes.slink;

import static app.revanced.integrations.shared.Utils.showToastShort;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import app.revanced.integrations.shared.Logger;

public interface IFixSLinksPatch {
    default String performResolution(Context context, String link) {
        if (link.matches(".*reddit\\.com/r/[^/]+/s/[^/]+")) {
            Logger.printInfo(() -> "Resolving " + link);
            try {
                HttpURLConnection connection = getHttpURLConnection(context, link);

                // Disable strict mode in order to allow network access on the main thread.
                // This is not ideal, but it's the easiest solution for now.
                final var currentPolicy = StrictMode.getThreadPolicy();
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                connection.connect();
                String location = connection.getHeaderField("location");
                connection.disconnect();

                // Restore the original strict mode policy.
                StrictMode.setThreadPolicy(currentPolicy);

                Logger.printInfo(() -> "Resolved " + link + " -> " + location);
                // For some reason using requireNotNull or similar ends up in java.lang.ExceptionInInitializerError,
                // despite exception being caught down below?
                if (location == null) {
                    Toast.makeText(context, "Can't open /s/ link - reddit didn't return valid response. Are you logged in?",
                            Toast.LENGTH_SHORT).show();
                    Logger.printInfo(() -> "Location is null - returning link.");
                    return link;
                }
                return location;
            } catch (SocketTimeoutException e) {
                Logger.printInfo(() -> "Reddit request timeout. Censored network?");
                Toast.makeText(context, "Can't open /s/ link - reddit request timed out.",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Logger.printException(() -> "Failed to resolve " + link, e);
                Toast.makeText(context, "Can't open /s/ link - unknown error occurred.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        return link;
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(Context context, String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Auth token is needed if users IP is blacklisted by reddit, e.g. VPN
        String accessToken = getUserAccessToken(context);
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("HEAD");
        // In case network has censors and blocks request to reddit we won't desire to become ANR
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        return connection;
    }

    @Nullable
    default String getUserAccessToken(Context context) {
        return null;
    }

    ;
}
