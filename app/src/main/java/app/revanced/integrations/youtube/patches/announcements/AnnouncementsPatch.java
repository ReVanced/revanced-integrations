package app.revanced.integrations.youtube.patches.announcements;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.youtube.patches.announcements.requests.AnnouncementsRoutes;
import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.integrations.youtube.patches.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENT;

public final class AnnouncementsPatch {
    private final static String CONSUMER = getOrSetConsumer();

    private AnnouncementsPatch() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showAnnouncement(final Activity context) {
        if (!Settings.ANNOUNCEMENTS.getBoolean()) return;

        // Check if there is internet connection
        if (!Utils.isNetworkConnected()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                HttpURLConnection connection = AnnouncementsRoutes.getAnnouncementsConnectionFromRoute(GET_LATEST_ANNOUNCEMENT, CONSUMER);

                Logger.printDebug(() -> "Get latest announcement route connection url: " + connection.getURL().toString());

                try {
                    // Do not show the announcement if the request failed.
                    if (connection.getResponseCode() != 200) {
                        if (Settings.ANNOUNCEMENT_LAST_HASH.getString().isEmpty()) return;

                        Settings.ANNOUNCEMENT_LAST_HASH.resetToDefault();
                        Utils.showToastLong("Failed to get announcement");

                        return;
                    }
                } catch (IOException ex) {
                    final var message = "Failed connecting to announcements provider";

                    Logger.printException(() -> message, ex);
                    return;
                }

                var jsonString = Requester.parseInputStreamAndClose(connection.getInputStream(), false);

                // Do not show the announcement if it is older or the same as the last one.
                final byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(jsonString.getBytes(StandardCharsets.UTF_8));
                final var hash = java.util.Base64.getEncoder().encodeToString(hashBytes);
                if (hash.equals(Settings.ANNOUNCEMENT_LAST_HASH.getString())) return;

                // Parse the announcement. Fall-back to raw string if it fails.
                String title;
                String message;
                Level level = Level.INFO;
                try {
                    final var announcement = new JSONObject(jsonString);

                    title = announcement.getString("title");
                    message = announcement.getJSONObject("content").getString("message");

                    if (!announcement.isNull("level")) level = Level.fromInt(announcement.getInt("level"));
                } catch (Throwable ex) {
                    Logger.printException(() -> "Failed to parse announcement. Fall-backing to raw string", ex);

                    title = "Announcement";
                    message = jsonString;
                }

                final var finalTitle = title;
                final var finalMessage = Html.fromHtml(message, FROM_HTML_MODE_COMPACT);
                final Level finalLevel = level;

                Utils.runOnMainThread(() -> {
                    // Show the announcement.
                    var alertDialog = new android.app.AlertDialog.Builder(context)
                            .setTitle(finalTitle)
                            .setMessage(finalMessage)
                            .setIcon(finalLevel.icon)
                            .setPositiveButton("Ok", (dialog, which) -> {
                                Settings.ANNOUNCEMENT_LAST_HASH.saveValue(hash);
                                dialog.dismiss();
                            }).setNegativeButton("Dismiss", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();

                    // Make links clickable.
                    ((TextView)alertDialog.findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());
                });
            } catch (Exception e) {
                final var message = "Failed to get announcement";

                Logger.printException(() -> message, e);
            }
        });
    }

    /**
     * Clears the last announcement hash if it is not empty.
     *
     * @return true if the last announcement hash was empty.
     */
    private static boolean emptyLastAnnouncementHash() {
        if (Settings.ANNOUNCEMENT_LAST_HASH.getString().isEmpty()) return true;
        Settings.ANNOUNCEMENT_LAST_HASH.resetToDefault();

        return false;
    }

    private static String getOrSetConsumer() {
        final var consumer = Settings.ANNOUNCEMENT_CONSUMER.getString();
        if (!consumer.isEmpty()) return consumer;

        final var uuid = UUID.randomUUID().toString();
        Settings.ANNOUNCEMENT_CONSUMER.saveValue(uuid);
        return uuid;
    }

    // TODO: Use better icons.
    private enum Level {
        INFO(android.R.drawable.ic_dialog_info),
        WARNING(android.R.drawable.ic_dialog_alert),
        SEVERE(android.R.drawable.ic_dialog_alert);

        public final int icon;

        Level(int icon) {
            this.icon = icon;
        }

        public static Level fromInt(int value) {
            return values()[Math.min(value, values().length - 1)];
        }
    }
}
