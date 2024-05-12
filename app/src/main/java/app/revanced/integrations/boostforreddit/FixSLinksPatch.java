package app.revanced.integrations.boostforreddit;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.fixes.slink.IFixSLinksPatch;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public final class FixSLinksPatch implements IFixSLinksPatch {
    private static IFixSLinksPatch INSTANCE;
    String accessToken = null;

    private FixSLinksPatch() {
    }

    public static IFixSLinksPatch getInstance() {
        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
        return INSTANCE;
    }

    public static String resolveSLink(Context context, String link) {
        return getInstance().performResolution(context, link);
    }

    @Override
    @Nullable
    public String getUserAccessToken(Context context) {
        if (accessToken == null) {
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
            accessToken = tokenCursor.getString(0);
            tokenCursor.close();
            redditDb.close();
            Logger.printInfo(() -> "Got token!");
        }
        return accessToken;
    }

}
