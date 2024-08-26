package app.revanced.integrations.youtube.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.patches.spoof.ClientType;
import app.revanced.integrations.youtube.patches.spoof.StoryboardRenderer;
import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.List;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.spoof.requests.PlayerRoutes.*;

@Deprecated
public class StoryboardRendererRequester {

    private StoryboardRendererRequester() {
    }

    private static void randomlyWaitIfLocallyDebugging() {
        final boolean randomlyWait = false; // Enable to simulate slow connection responses.
        if (randomlyWait) {
            final long maximumTimeToRandomlyWait = 10000;
            Utils.doNothingForDuration(maximumTimeToRandomlyWait);
        }
    }

    private static void handleConnectionError(@NonNull String toastMessage, @Nullable Exception ex,
                                              boolean showToastOnIOException) {
        if (showToastOnIOException) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    @Nullable
    private static JSONObject fetchPlayerResponse(@NonNull String requestBody, ClientType clientType, boolean showToastOnIOException) {
        final long startTime = System.currentTimeMillis();
        try {
            Utils.verifyOffMainThread();
            Objects.requireNonNull(requestBody);

            final byte[] innerTubeBody = requestBody.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STORYBOARD_SPEC_RENDERER, clientType);
            connection.setRequestProperty("User-Agent", ClientType.ANDROID.userAgent);
            connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);

            final int responseCode = connection.getResponseCode();
            randomlyWaitIfLocallyDebugging();
            if (responseCode == 200) return Requester.parseJSONObject(connection);

            // Always show a toast for this, as a non 200 response means something is broken.
            // Not a normal code path and should not be reached, so no translations are needed.
            handleConnectionError("Spoof storyboard not available: " + responseCode,
                    null, showToastOnIOException || BaseSettings.DEBUG_TOAST_ON_ERROR.get());
            connection.disconnect();
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("revanced_spoof_client_storyboard_timeout"), ex, showToastOnIOException);
        } catch (IOException ex) {
            handleConnectionError(str("revanced_spoof_client_storyboard_io_exception", ex.getMessage()),
                    ex, showToastOnIOException);
        } catch (Exception ex) {
            Logger.printException(() -> "Spoof storyboard fetch failed", ex); // Should never happen.
        } finally {
            Logger.printDebug(() -> "Request took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    private static boolean isPlayabilityStatusOk(@NonNull JSONObject playerResponse) {
        try {
            return playerResponse.getJSONObject("playabilityStatus").getString("status").equals("OK");
        } catch (JSONException e) {
            Logger.printDebug(() -> "Failed to get playabilityStatus for response: " + playerResponse);
        }

        return false;
    }

    /**
     * Fetches the storyboardRenderer from the innerTubeBody.
     * @return StoryboardRenderer or null if playabilityStatus is not OK.
     */
    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingBody(String videoId,
                                                                     ClientType clientType,
                                                                     boolean showToastOnIOException) {
        String innerTubeBody = String.format(PlayerRoutes.createInnertubeBody(clientType), videoId);
        final JSONObject playerResponse = fetchPlayerResponse(innerTubeBody, clientType, showToastOnIOException);
        if (playerResponse != null && isPlayabilityStatusOk(playerResponse))
            return getStoryboardRendererUsingResponse(videoId, playerResponse);

        return null;
    }

    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingResponse(@NonNull String videoId, @NonNull JSONObject playerResponse) {
        try {
            Logger.printDebug(() -> "Parsing response: " + playerResponse);
            if (!playerResponse.has("storyboards")) {
                Logger.printDebug(() -> "Using empty storyboard");
                return new StoryboardRenderer(videoId, null, false, null);
            }
            final JSONObject storyboards = playerResponse.getJSONObject("storyboards");
            final boolean isLiveStream = storyboards.has("playerLiveStoryboardSpecRenderer");
            final String storyboardsRendererTag = isLiveStream
                    ? "playerLiveStoryboardSpecRenderer"
                    : "playerStoryboardSpecRenderer";

            final var rendererElement = storyboards.getJSONObject(storyboardsRendererTag);
            StoryboardRenderer renderer = new StoryboardRenderer(
                    videoId,
                    rendererElement.getString("spec"),
                    isLiveStream,
                    rendererElement.has("recommendedLevel")
                            ? rendererElement.getInt("recommendedLevel")
                            : null
            );

            Logger.printDebug(() -> "Fetched: " + renderer);

            return renderer;
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to get storyboardRenderer", e);
        }

        return null;
    }

    @Nullable
    public static StoryboardRenderer getStoryboardRenderer(@NonNull String videoId) {
        Objects.requireNonNull(videoId);

        StoryboardRenderer renderer = null;

        List<ClientType> clientTypeList = List.of(
                ClientType.ANDROID,
                ClientType.ANDROID_VR
        );

        for (ClientType clientType : clientTypeList) {
            renderer = getStoryboardRendererUsingBody(videoId, clientType, false);
            if (renderer != null)  break;
        }

        if (renderer == null) {
            Logger.printDebug(() -> videoId + " not available");
        }

        return renderer;
    }
}