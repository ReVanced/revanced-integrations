package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

public class ClientSpoofPatch {
    private static final Uri LOCALHOST_URI = Uri.parse("https://127.0.0.1/");

    /**
     * Injection point.
     * Blocks /get_watch requests by returning a localhost URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return Localhost URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        return playerRequestUri.getPath().contains("get_watch") ? LOCALHOST_URI : playerRequestUri;
    }
}
