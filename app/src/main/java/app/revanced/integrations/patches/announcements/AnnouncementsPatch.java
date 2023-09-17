package app.revanced.integrations.patches.announcements;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.patches.announcements.requests.AnnouncementsRoutes;
import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static app.revanced.integrations.patches.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENT;

public final class AnnouncementsPatch {
    private final static String CONSUMER = getOrSetConsumer();

    private AnnouncementsPatch() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showAnnouncement(final Activity context) {
        if (!SettingsEnum.ANNOUNCEMENTS.getBoolean()) return;

        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                HttpURLConnection connection = AnnouncementsRoutes.getAnnouncementsConnectionFromRoute(GET_LATEST_ANNOUNCEMENT, CONSUMER);

                LogHelper.printDebug(() -> "Get latest announcement route connection url: " + connection.getURL().toString());

                try {
                    // Do not show the announcement if the request failed.
                    if (connection.getResponseCode() != 200) {
                        if (SettingsEnum.LAST_ANNOUNCEMENT_HASH.getString().isEmpty()) return;

                        SettingsEnum.LAST_ANNOUNCEMENT_HASH.saveValue("");
                        ReVancedUtils.showToastLong("Failed to get announcement");

                        return;
                    }
                } catch (IOException ex) {
                    final var message = "Failed connecting to announcements provider";

                    LogHelper.printException(() -> message, ex);
                    return;
                }

                var jsonString = Requester.parseInputStreamAndClose(connection.getInputStream(), false);

                // Do not show the announcement if it is older or the same as the last one.
                final byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(jsonString.getBytes(StandardCharsets.UTF_8));
                final var hash = java.util.Base64.getEncoder().encodeToString(hashBytes);
                if (hash.equals(SettingsEnum.LAST_ANNOUNCEMENT_HASH.getString())) return;

                // Parse the announcement. Fall-back to raw string if it fails.
                String title;
                String message;
                try {
                    final var announcement = new JSONObject(jsonString);

                    title = announcement.getString("title");
                    message = announcement.getJSONObject("content").getString("message");
                } catch (Throwable ex) {
                    LogHelper.printException(() -> "Failed to parse announcement. Fall-backing to raw string", ex);

                    title = "Announcement";
                    message = jsonString;
                }

                final var finalTitle = title;
                final var finalMessage = message;

                ReVancedUtils.runOnMainThread(() -> {
                    // Show the announcement.
                    new android.app.AlertDialog.Builder(context).setTitle(finalTitle)
                            .setMessage(finalMessage)
                            .setPositiveButton("Ok", (dialog, which) -> {
                                SettingsEnum.LAST_ANNOUNCEMENT_HASH.saveValue(hash);
                                dialog.dismiss();
                            }).setNegativeButton("Dismiss", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                });
            } catch (Exception e) {
                final var message = "Failed to get announcement";

                LogHelper.printException(() -> message, e);
            }
        });
    }

    /**
     * Clears the last announcement hash if it is not empty.
     *
     * @return true if the last announcement hash was empty.
     */
    private static boolean emptyLastAnnouncementHash() {
        if (SettingsEnum.LAST_ANNOUNCEMENT_HASH.getString().isEmpty()) return true;
        SettingsEnum.LAST_ANNOUNCEMENT_HASH.saveValue("");

        return false;
    }

    private static String getOrSetConsumer() {
        final var consumer = SettingsEnum.ANNOUNCEMENT_CONSUMER.getString();
        if (!consumer.isEmpty()) return consumer;

        final var uuid = UUID.randomUUID().toString();
        SettingsEnum.ANNOUNCEMENT_CONSUMER.saveValue(uuid);
        return uuid;
    }
}
