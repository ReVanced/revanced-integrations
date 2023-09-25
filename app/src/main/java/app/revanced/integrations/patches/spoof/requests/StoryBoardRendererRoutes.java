package app.revanced.integrations.patches.spoof.requests;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;

import java.io.IOException;
import java.net.HttpURLConnection;

final class StoryBoardRendererRoutes {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";
    static final String YT_API_KEY = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w";

    static final Route GET_PLAYER = new Route(Route.Method.POST, "player?key={api_key}");

    private StoryBoardRendererRoutes() {
    }

    /** @noinspection SameParameterValue*/
    static HttpURLConnection getPlayerResponseConnectionFromRoute(Route route, String... params) throws IOException {
        var connection = Requester.getConnectionFromRoute(YT_API_URL, route, params);
        connection.setRequestProperty("User-Agent", "com.google.android.youtube/18.37.36 (Linux; U; Android 12; GB) gzip");
        connection.setRequestProperty("X-Goog-Api-Format-Version", "2");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept-Language", "en-GB, en;q=0.9");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }
}