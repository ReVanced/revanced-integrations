package app.revanced.integrations.youtube.patches.spoof.requests;

import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.requests.Route;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

final class PlayerRoutes {
    private static final String YT_API_URL = "https://youtubei.googleapis.com/youtubei/v1/";

    static final Route.CompiledRoute GET_STORYBOARD_SPEC_RENDERER = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=storyboards.playerStoryboardSpecRenderer," +
                    "storyboards.playerLiveStoryboardSpecRenderer," +
                    "playabilityStatus.status"
    ).compile();

    static final Route.CompiledRoute GET_STREAMING_DATA = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=streamingData" +
                    "&alt=proto"
    ).compile();
    
    static final String ANDROID_INNER_TUBE_BODY;
    static final String VR_INNER_TUBE_BODY;
    static final String UNPLUGGED_INNER_TUBE_BODY;
    static final String TESTSUITE_INNER_TUBE_BODY;
    static final String TV_EMBED_INNER_TUBE_BODY;

    /**
     * TCP connection and HTTP read timeout
     */
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 Seconds.

    static {
        JSONObject innerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "ANDROID");
            client.put("clientVersion", Utils.getAppVersionName());
            client.put("androidSdkVersion", 34);

            context.put("client", client);

            innerTubeBody.put("context", context);
            innerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create innerTubeBody", e);
        }

        ANDROID_INNER_TUBE_BODY = innerTubeBody.toString();

        JSONObject vrInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "ANDROID_VR");
            client.put("clientVersion", "1.58.14");
            client.put("deviceModel", "Quest 3");
            client.put("osVersion", "12");
            client.put("androidSdkVersion", 34);

            context.put("client", client);

            vrInnerTubeBody.put("contentCheckOk", true);
            vrInnerTubeBody.put("racyCheckOk", true);
            vrInnerTubeBody.put("context", context);
            vrInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create vrInnerTubeBody", e);
        }

        VR_INNER_TUBE_BODY = vrInnerTubeBody.toString();

        JSONObject unpluggedInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "ANDROID_UNPLUGGED");
            client.put("clientVersion", "8.33.0");
            client.put("deviceModel", "ADT-3");
            client.put("osVersion", "14");
            client.put("androidSdkVersion", 34);

            context.put("client", client);

            unpluggedInnerTubeBody.put("contentCheckOk", true);
            unpluggedInnerTubeBody.put("racyCheckOk", true);
            unpluggedInnerTubeBody.put("context", context);
            unpluggedInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create unpluggedInnerTubeBody", e);
        }

        UNPLUGGED_INNER_TUBE_BODY = unpluggedInnerTubeBody.toString();

        JSONObject suiteInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "ANDROID_TESTSUITE");
            client.put("clientVersion", "1.9");
            client.put("deviceModel", "Pixel 8 Pro");
            client.put("osVersion", "14");
            client.put("androidSdkVersion", 34);

            context.put("client", client);

            suiteInnerTubeBody.put("contentCheckOk", true);
            suiteInnerTubeBody.put("racyCheckOk", true);
            suiteInnerTubeBody.put("context", context);
            suiteInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create suiteInnerTubeBody", e);
        }

        TESTSUITE_INNER_TUBE_BODY = suiteInnerTubeBody.toString();

        JSONObject tvEmbedInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER");
            client.put("clientVersion", "2.0");
            client.put("platform", "TV");
            client.put("clientScreen", "EMBED");

            JSONObject thirdParty = new JSONObject();
            thirdParty.put("embedUrl", "https://www.youtube.com/watch?v=%s");

            context.put("thirdParty", thirdParty);
            context.put("client", client);

            tvEmbedInnerTubeBody.put("context", context);
            tvEmbedInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create tvEmbedInnerTubeBody", e);
        }

        TV_EMBED_INNER_TUBE_BODY = tvEmbedInnerTubeBody.toString();
    }

    private PlayerRoutes() {
    }

    /** @noinspection SameParameterValue*/
    static HttpURLConnection getPlayerResponseConnectionFromRoute(Route.CompiledRoute route) throws IOException {
        var connection = Requester.getConnectionFromCompiledRoute(YT_API_URL, route);

        connection.setRequestProperty(
                "User-Agent", "com.google.android.youtube/" +
                        Utils.getAppVersionName() +
                        " (Linux; U; Android 12; GB) gzip"
        );
        connection.setRequestProperty("X-Goog-Api-Format-Version", "2");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        connection.setReadTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        return connection;
    }
}