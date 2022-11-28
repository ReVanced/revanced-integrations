package app.revanced.integrations.returnyoutubedislike.requests;

import static app.revanced.integrations.requests.Requester.parseJson;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;
import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReturnYouTubeDislikeApi {
    private static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    /**
     * Default connection and response timeout for {@link #fetchDislikes(String)}
     */
    private static final int API_GET_DISLIKE_DEFAULT_TIMEOUT_MILLISECONDS = 5000;

    /**
     * Default connection and response timeout for voting and registration.
     *
     * Voting and user registration runs in the background and has has no urgency
     * so this can be a larger value.
     */
    private static final int API_REGISTER_VOTE_DEFAULT_TIMEOUT_MILLISECONDS = 90000;

    /**
     * Response code of a successful API call
     */
    private static final int SUCCESS_HTTP_STATUS_CODE = 200;

    /**
     * Indicates a client rate limit has been reached
     */
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

    private ReturnYouTubeDislikeApi() {
    } // utility class

    /**
     * Only for local debugging to simulate a slow api call.
     * Does this by doing meaningless calculations.
     *
     * @param maximumTimeToWait maximum time to wait
     */
    private static long randomlyWaitIfLocallyDebugging(long maximumTimeToWait) {
        final boolean DEBUG_RANDOMLY_DELAY_NETWORK_CALLS = false;
        if (DEBUG_RANDOMLY_DELAY_NETWORK_CALLS) {
            final long amountOfTimeToWaste = (long) (Math.random() * maximumTimeToWait);
            final long timeCalculationStarted = System.currentTimeMillis();
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Artificially creating network delay of: " + amountOfTimeToWaste + " ms");

            long meaninglessValue = 0;
            while (System.currentTimeMillis() - timeCalculationStarted < amountOfTimeToWaste) {
                meaninglessValue += Long.numberOfLeadingZeros((long)(Math.random() * Long.MAX_VALUE));
            }
            // return the value, otherwise the compiler or VM might optimize and remove the meaningless time wasting work,
            // leaving an empty loop that hammers on the System.currentTimeMillis native call
            return meaninglessValue;
        }
        return 0;
    }

    /**
     * @return True, if api rate limit is in effect.
     */
    private static boolean checkIfRateLimitInEffect(String apiEndPointName) {
        if (lastTimeLimitWasHit == 0) {
            return false;
        }
        final long numberOfSecondsSinceLastRateLimit = (System.currentTimeMillis() - lastTimeLimitWasHit) / 1000;
        if (numberOfSecondsSinceLastRateLimit < RATE_LIMIT_BACKOFF_SECONDS) {
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Ignoring api call " + apiEndPointName + " as only "
                    + numberOfSecondsSinceLastRateLimit + " seconds has passed since last rate limit.");
            return true;
        }
        return false;
    }

    /**
     * @return True, if the rate limit was reached.
     */
    private static boolean checkIfRateLimitWasHit(int httpResponseCode) {
        // set to true, to verify rate limit works
        final boolean DEBUG_RATE_LIMIT = false;
        if (DEBUG_RATE_LIMIT) {
            final double RANDOM_RATE_LIMIT_PERCENTAGE = 0.1; // 10% chance of a triggering a rate limit
            if (Math.random() < RANDOM_RATE_LIMIT_PERCENTAGE) {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Artificially triggering rate limit for debug purposes");
                httpResponseCode = RATE_LIMIT_HTTP_STATUS_CODE;
            }
        }

        if (httpResponseCode == RATE_LIMIT_HTTP_STATUS_CODE) {
            lastTimeLimitWasHit = System.currentTimeMillis();
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "API rate limit was hit. Stopping API calls for the next "
                    + RATE_LIMIT_BACKOFF_SECONDS + " seconds");
            return true;
        }
        return false;
    }

    /**
     * @return The number of dislikes.
     * Returns NULL if fetch failed, calling thread is interrupted, or rate limit is in effect.
     */
    @Nullable
    public static Integer fetchDislikes(String videoId) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        try {
            if (checkIfRateLimitInEffect("fetchDislikes")) {
                return null;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Fetching dislikes for: " + videoId);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_DISLIKES, videoId);
            connection.setConnectTimeout(API_GET_DISLIKE_DEFAULT_TIMEOUT_MILLISECONDS); // timeout for TCP connection to server
            connection.setReadTimeout(API_GET_DISLIKE_DEFAULT_TIMEOUT_MILLISECONDS); // timeout for server response
            randomlyWaitIfLocallyDebugging(2*API_GET_DISLIKE_DEFAULT_TIMEOUT_MILLISECONDS);

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect();
                return null;
            }
            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                JSONObject json = getJSONObjectAndDisconnect(connection);
                Integer fetchedDislikeCount = json.getInt("dislikes");
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Fetched video: " + videoId
                        + " dislikes: " + fetchedDislikeCount);
                return fetchedDislikeCount;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to fetch dislikes for video: " + videoId
                    + " response code was: " + responseCode);
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to fetch dislikes", ex);
        }
        return null;
    }

    /**
     * @return The newly created and registered user id.  Returns NULL if registration failed.
     */
    @Nullable
    public static String registerAsNewUser() {
        ReVancedUtils.verifyOffMainThread();
        try {
            if (checkIfRateLimitInEffect("registerAsNewUser")) {
                return null;
            }
            String userId = randomString(36);
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Trying to register new user: " + userId);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_REGISTRATION, userId);
            connection.setConnectTimeout(API_REGISTER_VOTE_DEFAULT_TIMEOUT_MILLISECONDS);
            connection.setReadTimeout(API_REGISTER_VOTE_DEFAULT_TIMEOUT_MILLISECONDS);

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect();
                return null;
            }
            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                JSONObject json = getJSONObjectAndDisconnect(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");

                String solution = solvePuzzle(challenge, difficulty);
                return confirmRegistration(userId, solution);
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to register new user: " + userId
                    + " response code was: " + responseCode);
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to register user", ex);
        }
        return null;
    }

    @Nullable
    private static String confirmRegistration(String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);
        try {
            if (checkIfRateLimitInEffect("confirmRegistration")) {
                return null;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Trying to confirm registration for user: " + userId + " with solution: " + solution);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonPostRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect();
                return null;
            }
            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                String result = parseJson(connection); // also disconnects
                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration confirmation successful for user: " + userId);
                    return userId;
                }
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to confirm registration for user: " + userId
                        + " solution: " + solution + " response string was: " + result);
                return null;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to confirm registration for user: " + userId
                    + " solution: " + solution + " response code was: " + responseCode);
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to confirm registration for user: " + userId
                    + "solution: " + solution, ex);
        }

        return null;
    }

    public static boolean sendVote(String videoId, String userId, ReturnYouTubeDislike.Vote vote) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(vote);

        try {
            if (checkIfRateLimitInEffect("sendVote")) {
                return false;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Trying to vote for video: "
                    + videoId + " with vote: " + vote + " user: " + userId);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.SEND_VOTE);
            applyCommonPostRequestSettings(connection);

            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote.value + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect();
                return false;
            }
            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                JSONObject json = getJSONObjectAndDisconnect(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");

                String solution = solvePuzzle(challenge, difficulty);
                return confirmVote(videoId, userId, solution);
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to send vote for video: " + videoId
                    + " userId: " + userId + " vote: " + vote + " response code was: " + responseCode);
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class,"Failed to send vote for video: " + videoId
                    + " user: " + userId + " vote: " + vote, ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);

        try {
            if (checkIfRateLimitInEffect("confirmVote")) {
                return false;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Trying to confirm vote for video: "
                    + videoId + " user: " + userId + " solution: " + solution);
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_VOTE);
            applyCommonPostRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect();
                return false;
            }

            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                String result = parseJson(connection); // also disconnects
                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote confirm successful for video: " + videoId);
                    return true;
                }
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to confirm vote for video: " + videoId
                        + " user: " + userId + " solution: " + solution + " response string was: " + result);
                return false;
            }
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Failed to confirm vote for video: " + videoId
                    + " user: " + userId + " solution: " + solution + " response code was: " + responseCode);
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to confirm vote for video: " + videoId
                    + " user: " + userId + " solution: " + solution, ex);
        }
        return false;
    }

    // utils

    private static void applyCommonPostRequestSettings(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(API_REGISTER_VOTE_DEFAULT_TIMEOUT_MILLISECONDS); // timeout for TCP connection to server
        connection.setReadTimeout(API_REGISTER_VOTE_DEFAULT_TIMEOUT_MILLISECONDS); // timeout for server response
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(RYD_API_URL, route, params);
    }

    private static JSONObject getJSONObjectAndDisconnect(HttpURLConnection connection) throws JSONException, IOException {
        return Requester.getJSONObject(connection);
    }

    private static String solvePuzzle(String challenge, int difficulty) {
        final long timeSolveStarted = System.currentTimeMillis();
        byte[] decodedChallenge = Base64.decode(challenge, Base64.NO_WRAP);

        byte[] buffer = new byte[20];
        for (int i = 4; i < 20; i++) {
            buffer[i] = decodedChallenge[i - 4];
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex); // should never happen
        }

        final int maxCount = (int) (Math.pow(2, difficulty + 1) * 5);
        for (int i = 0; i < maxCount; i++) {
            buffer[0] = (byte) i;
            buffer[1] = (byte) (i >> 8);
            buffer[2] = (byte) (i >> 16);
            buffer[3] = (byte) (i >> 24);
            byte[] messageDigest = md.digest(buffer);

            if (countLeadingZeroes(messageDigest) >= difficulty) {
                String solution = Base64.encodeToString(new byte[]{buffer[0], buffer[1], buffer[2], buffer[3]}, Base64.NO_WRAP);
                LogHelper.debug(ReturnYouTubeDislikeApi.class,
                        "Found puzzle solution: " + solution + " of difficulty: " + difficulty
                                + " in: " + (System.currentTimeMillis() - timeSolveStarted) + " ms");
                return solution;
            }
        }

        // should never be reached
        throw new IllegalStateException("Failed to solve puzzle challenge: " + challenge + " of difficulty: " + difficulty);
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
