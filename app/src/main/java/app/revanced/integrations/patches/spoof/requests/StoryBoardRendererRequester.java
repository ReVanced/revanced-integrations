package app.revanced.integrations.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.patches.spoof.SpoofSignaturePatch;
import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class StoryBoardRendererRequester {
    private static final String INNER_TUBE_BODY =
            "{" +
                    "\"context\": " +
                    "{" +
                    "\"client\": " +
                    "{ " +
                    "\"clientName\": \"ANDROID\", \"clientVersion\": \"18.37.36\", \"platform\": \"MOBILE\", " +
                    "\"osName\": \"Android\", \"osVersion\": \"12\", \"androidSdkVersion\": 31 " +
                    "} " +
                    "}, " +
                    "\"videoId\": \"%s\"" +
                    "}";

    private StoryBoardRendererRequester() {
    }

    // TODO: Find a way to increase the quality of seekbar thumbnail previews.
    @Nullable
    public static String fetchStoryboardsRenderer(@NonNull String videoId) {
        try {
            ReVancedUtils.verifyOffMainThread();

            final byte[] innerTubeBody = String.format(INNER_TUBE_BODY, videoId).getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = StoryBoardRendererRoutes.getPlayerResponseConnectionFromRoute();
            connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);

            final int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                final JSONObject playerResponse = Requester.parseJSONObject(connection);

                final JSONObject storyboards = playerResponse.getJSONObject("storyboards");
                final String storyboardsRendererTag = storyboards.has("playerLiveStoryboardSpecRenderer")
                        ? "playerLiveStoryboardSpecRenderer"
                        : "playerStoryboardSpecRenderer";
                final JSONObject storyboardsRenderer = storyboards.getJSONObject(storyboardsRendererTag);
                final String storyboardsRendererSpec = storyboardsRenderer.getString("spec");

                SpoofSignaturePatch.setStoryboardRendererSpec(storyboardsRendererSpec);
                SpoofSignaturePatch.setRecommendedLevel(storyboardsRenderer.getInt("recommendedLevel"));

                LogHelper.printDebug(() -> "StoryBoard renderer spec: " + storyboardsRendererSpec);

            } else {
                LogHelper.printException(() -> "API not available: " + responseCode);
                connection.disconnect();
            }
        } catch (SocketTimeoutException ex) {
            LogHelper.printException(() -> "API timed out", ex);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to fetch StoryBoard URL", ex);
        }

        return null;
    }
}