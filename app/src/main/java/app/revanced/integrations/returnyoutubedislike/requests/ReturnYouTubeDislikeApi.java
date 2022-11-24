package app.revanced.integrations.returnyoutubedislike.requests;

import static app.revanced.integrations.requests.Requester.parseJson;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Objects;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;
import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReturnYouTubeDislikeApi {
    private static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    private static final int HTTP_CONNECTION_DEFAULT_TIMEOUT = 5000;

    /** Indicates a client rate limit has been reached */
    private static final int RATE_LIMIT_HTTP_STATUS_CODE = 429;

    /**
     * How long wait until API calls are resumed, if a rate limit is hit
     * No clear guideline of how long to backoff.  Using 60 seconds for now.
     */
    private static final int RATE_LIMIT_BACKOFF_SECONDS = 60;

    /**
     * Last time a {@link #RATE_LIMIT_HTTP_STATUS_CODE} was reached.
     * zero if has not been reached.
     */
    private static volatile long lastTimeLimitWasHit; // must be volatile, since different threads access this

    private ReturnYouTubeDislikeApi() { } // utility class

    /**
     * @return true, if api rate limit is in effect
     */
    private static boolean rateLimitInEffect(String apiEndPointName) {
        if (lastTimeLimitWasHit == 0) {
            return false;
        }
        final long numberOfSecondsSinceLastRateLimit = (System.currentTimeMillis() - lastTimeLimitWasHit) / 1000;
        if (numberOfSecondsSinceLastRateLimit > RATE_LIMIT_BACKOFF_SECONDS) {
            LogHelper.debug(()->"Ignoring api call " + apiEndPointName + " as only "
                    + numberOfSecondsSinceLastRateLimit + " has passed since last rate limit");
            return true;
        }
        return false;
    }

    /**
     * @return true, if the rate limit was reached
     */
    private static boolean checkIfRateLimitWasHit(int httpResponseCode, String apiEndPointName) {
        if (httpResponseCode == RATE_LIMIT_HTTP_STATUS_CODE) {
            lastTimeLimitWasHit = System.currentTimeMillis();
            LogHelper.debug(() -> "API rate limit was hit.  Stopping API calls for the next "
                                   + RATE_LIMIT_BACKOFF_SECONDS + " seconds");
            return true;
        }
        return false;
    }

    /**
     * @return returns NULL if fetch failed or if calling thread is interrupted
     */
    public static Integer fetchDislikes(String videoId) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        try {
            String apiEndPointName = "fetchDislikes";
            if (rateLimitInEffect(apiEndPointName)) {
                return null;
            }
            LogHelper.debug(()->"Fetching dislikes for " + videoId);
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_DISLIKES, videoId);
            connection.setConnectTimeout(HTTP_CONNECTION_DEFAULT_TIMEOUT);
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode, apiEndPointName)) {
                connection.disconnect();
                return null;
            } else if (responseCode == 200) {
                JSONObject json = getJSONObject(connection);
                Integer fetchedDislikeCount = json.getInt("dislikes");
                LogHelper.debug(()->"dislikes fetched - " + fetchedDislikeCount);
                connection.disconnect();
                return fetchedDislikeCount;
            } else {
                LogHelper.debug(()->"dislikes fetch response was " + responseCode);
                connection.disconnect();
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to fetch dislikes", ex);
        }
        return null;
    }

    /**
     * @return the newly created and registered user id.  Returns NULL if registration failed.
     */
    public static @Nullable String registerAsNewUser() {
        ReVancedUtils.verifyOffMainThread();
        try {
            String apiEndPointName = "registerAsNewUser";
            if (rateLimitInEffect(apiEndPointName)) {
                return null;
            }
            String userId = randomString(36);
            LogHelper.debug(()->"Trying to register the following userId: " + userId);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_REGISTRATION, userId);
            connection.setConnectTimeout(HTTP_CONNECTION_DEFAULT_TIMEOUT);
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode, apiEndPointName)) {
                connection.disconnect();
                return null;
            } else if (responseCode == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                LogHelper.debug(()->"Registration challenge - " + challenge + " with difficulty of " + difficulty);
                connection.disconnect();

                // Solve the puzzle
                String solution = solvePuzzle(challenge, difficulty);
                LogHelper.debug(()->"Registration confirmation solution is " + solution);
                if (solution == null) {
                    return null; // failed to solve puzzle
                }
                return confirmRegistration(userId, solution);
            } else {
                LogHelper.debug(()->"Registration response was " + responseCode);
                connection.disconnect();
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to register userId", ex);
        }
        return null;
    }
    private static String confirmRegistration(String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);
        try {
            String apiEndPointName = "confirmRegistration";
            if (rateLimitInEffect(apiEndPointName)) {
                return null;
            }
            LogHelper.debug(()->"Trying to confirm registration for the following userId: " + userId + " with solution: " + solution);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode, apiEndPointName)) {
                connection.disconnect();
                return null;
            } else if (responseCode == 200) {
                String result = parseJson(connection);
                LogHelper.debug(()->"Registration confirmation result was " + result);
                connection.disconnect();

                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug(()->"Registration was successful for user " + userId);
                    return userId;
                }
            } else {
                LogHelper.debug(()->"Registration confirmation response was " + responseCode);
                connection.disconnect();
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to confirm registration", ex);
        }

        return null;
    }

    public static boolean sendVote(String videoId, String userId, ReturnYouTubeDislike.Vote vote) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(vote);

        String apiEndPointName = "sendVote";
        if (rateLimitInEffect(apiEndPointName)) {
            return false;
        }
        LogHelper.debug(()->"Trying to vote the following video: "
                        + videoId + " with vote " + vote + " and userId: " + userId);
        try {
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.SEND_VOTE);
            applyCommonRequestSettings(connection);

            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote.value + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode, apiEndPointName)) {
                connection.disconnect();
                return false;
            } else if (responseCode == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                LogHelper.debug(()->"Vote challenge - " + challenge + " with difficulty of " + difficulty);
                connection.disconnect();

                // Solve the puzzle
                String solution = solvePuzzle(challenge, difficulty);
                LogHelper.debug(()->"Vote confirmation solution is " + solution);

                // Confirm vote
                return confirmVote(videoId, userId, solution);
            } else {
                LogHelper.debug(()->"Vote response was " + responseCode);
                connection.disconnect();
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to send vote", ex);
        }
        return false;
    }
    private static boolean confirmVote(String videoId, String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);

        String apiEndPointName = "confirmVote";
        if (rateLimitInEffect(apiEndPointName)) {
            return false;
        }
        try {
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_VOTE);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode, apiEndPointName)) {
                connection.disconnect();
                return false;
            } else if (responseCode == 200) {
                String result = parseJson(connection);
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote confirmation result was " + result);
                connection.disconnect();

                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug(()->"Vote was successful for user " + userId);
                    return true;
                } else {
                    LogHelper.debug(()->"Vote was unsuccessful for user " + userId);
                    return false;
                }
            } else {
                LogHelper.debug(()->"Vote confirmation response was " + responseCode);
                connection.disconnect();
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to confirm vote", ex);
        }
        return false;
    }

    // utils

    private static void applyCommonRequestSettings(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(HTTP_CONNECTION_DEFAULT_TIMEOUT);
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(RYD_API_URL, route, params);
    }

    private static JSONObject getJSONObject(HttpURLConnection connection) throws Exception {
        return Requester.getJSONObject(connection);
    }

    private static String solvePuzzle(String challenge, int difficulty) {
        byte[] decodedChallenge = Base64.decode(challenge, Base64.NO_WRAP);

        byte[] buffer = new byte[20];
        for (int i = 4; i < 20; i++) {
            buffer[i] = decodedChallenge[i - 4];
        }

        try {
            int maxCount = (int) (Math.pow(2, difficulty + 1) * 5);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            for (int i = 0; i < maxCount; i++) {
                buffer[0] = (byte) i;
                buffer[1] = (byte) (i >> 8);
                buffer[2] = (byte) (i >> 16);
                buffer[3] = (byte) (i >> 24);
                byte[] messageDigest = md.digest(buffer);

                if (countLeadingZeroes(messageDigest) >= difficulty) {
                    return Base64.encodeToString(new byte[]{buffer[0], buffer[1], buffer[2], buffer[3]}, Base64.NO_WRAP);
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(()->"Failed to solve puzzle", ex);
        }

        return null;
    }

    // https://stackoverflow.com/a/157202
    private static String randomString(int len) {
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private static int countLeadingZeroes(byte[] uInt8View) {
        int zeroes = 0;
        int value = 0;
        for (int i = 0; i < uInt8View.length; i++) {
            value = uInt8View[i] & 0xFF;
            if (value == 0) {
                zeroes += 8;
            } else {
                int count = 1;
                if (value >>> 4 == 0) {
                    count += 4;
                    value <<= 4;
                }
                if (value >>> 6 == 0) {
                    count += 2;
                    value <<= 2;
                }
                zeroes += count - (value >>> 7);
                break;
            }
        }
        return zeroes;
    }
}
