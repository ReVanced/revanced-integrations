package app.revanced.integrations.youtube.patches.announcements.requests;

import static app.revanced.integrations.youtube.requests.Route.Method.GET;

import java.io.IOException;
import java.net.HttpURLConnection;

import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.requests.Route;

public class AnnouncementsRoutes {
    private static final String ANNOUNCEMENTS_PROVIDER = "https://api.revanced.app/v2";

    /**
     * 'language' parameter is IETF format (for USA it would be 'en-us').
     */
    public static final Route GET_LATEST_ANNOUNCEMENT = new Route(GET, "/announcements/youtube/latest?language={language}");

    private AnnouncementsRoutes() {
    }

    public static HttpURLConnection getAnnouncementsConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(ANNOUNCEMENTS_PROVIDER, route, params);
    }
}