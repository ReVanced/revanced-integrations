package app.revanced.integrations.boostforreddit;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;

import app.revanced.integrations.shared.Logger;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public final class FixSLinksPatch {
    public static String getUserAccessToken(Context context) {
        File dbFile = context.getDatabasePath("reddit.db");
        if (!dbFile.exists()) {
            Logger.printInfo(() -> "Reddit database is not present at " + dbFile.getPath());
            return null;
        }
        SQLiteDatabase redditDb = SQLiteDatabase.openDatabase(
                dbFile.getPath(),
                null, SQLiteDatabase.OPEN_READONLY);
        Cursor tokenCursor = redditDb.rawQuery("SELECT AccessToken FROM User", null);
        if (tokenCursor.getCount() == 0) {
            Logger.printInfo(() -> "No authorized users found");
            return null;
        }
        tokenCursor.moveToFirst();
        String token = tokenCursor.getString(0);
        tokenCursor.close();
        redditDb.close();
        return token;
    }

    public static String resolveSLink(Context context, String link) {
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
                    Logger.printInfo(() -> "Location is null - returning link.");
                    return link;
                }
                return location;
            } catch (SocketTimeoutException e) {
                Logger.printInfo(() -> "Reddit request timeout. Censored network?");
            } catch (Exception e) {
                Logger.printException(() -> "Failed to resolve " + link, e);
            }
        }

        return link;
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection(Context context, String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "android:app.revanced.reddit:v1.0 (by /u/spez)");
        // Auth token is needed if users IP is blacklisted by reddit, e.g. VPN
        String accessToken = getUserAccessToken(context);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("HEAD");
        // In case network has censors and blocks request to reddit we won't desire to become ANR
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        return connection;
    }
}
