package app.revanced.integrations.sponsorblock.requests;

import static app.revanced.integrations.utils.StringRef.str;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.sponsorblock.objects.SegmentCategory;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment.SegmentVote;
import app.revanced.integrations.sponsorblock.objects.UserStats;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SBRequester {
    private static final String TIME_TEMPLATE = "%.3f";

    /**
     * TCP timeout
     */
    private static final int TIMEOUT_TCP_DEFAULT_MILLISECONDS = 7000;

    /**
     * HTTP response timeout
     */
    private static final int TIMEOUT_HTTP_DEFAULT_MILLISECONDS = 10000;

    /**
     * Response code of a successful API call
     */
    private static final int HTTP_STATUS_CODE_SUCCESS = 200;

    private SBRequester() {
    }

    private static void handleConnectionError(@NonNull String toastMessage, @Nullable Exception ex) {
        if (SettingsEnum.SB_TOAST_ON_CONNECTION_ERROR.getBoolean()) {
            ReVancedUtils.showToastShort(toastMessage);
        }
        if (ex != null) {
            LogHelper.printInfo(() -> toastMessage, ex);
        }
    }

    @NonNull
    public static SponsorSegment[] getSegments(@NonNull String videoId) {
        ReVancedUtils.verifyOffMainThread();
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.GET_SEGMENTS, videoId, SegmentCategory.sponsorBlockAPIFetchCategories);
            final int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                JSONArray responseArray = Requester.parseJSONArray(connection);
                final long minSegmentDuration = (long) (SettingsEnum.SB_SEGMENT_MIN_DURATION.getFloat() * 1000);
                for (int i = 0, length = responseArray.length(); i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    final long start = (long) (segment.getDouble(0) * 1000);
                    final long end = (long) (segment.getDouble(1) * 1000);

                    String uuid = obj.getString("UUID");
                    final boolean locked = obj.getInt("locked") == 1;
                    String categoryKey = obj.getString("category");
                    SegmentCategory category = SegmentCategory.byCategoryKey(categoryKey);
                    if (category == null) {
                        LogHelper.printException(() -> "Received unknown category: " + categoryKey); // should never happen
                    } else if ((end - start) >= minSegmentDuration || category == SegmentCategory.HIGHLIGHT) {
                        segments.add(new SponsorSegment(category, uuid, start, end, locked));
                    }
                }
                LogHelper.printDebug(() -> {
                    StringBuilder builder = new StringBuilder("Downloaded segments:");
                    for (SponsorSegment segment : segments) {
                        builder.append('\n').append(segment);
                    }
                    return builder.toString();
                });
                runVipCheckInBackgroundIfNeeded();
            } else if (responseCode == 404) {
                // no segments are found.  a normal response
                LogHelper.printDebug(() -> "No segments found for video: " + videoId);
            } else {
                handleConnectionError(str("sb_sponsorblock_connection_failure_status", responseCode), null);
                connection.disconnect(); // something went wrong, might as well disconnect
            }
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("sb_sponsorblock_connection_failure_timeout"), ex);
        } catch (IOException ex) {
            handleConnectionError(str("sb_sponsorblock_connection_failure_generic"), ex);
        } catch (Exception ex) {
            // Should never happen
            LogHelper.printException(() -> "getSegments failure", ex);
        }

        // Crude debug tests to verify random features
        // Could benefit from:
        // 1) collection of YouTube videos with test segment times (verify client skip timing matches the video, verify seekbar draws correctly)
        // 2) unit tests (verify everything else)
        if (false) {
            segments.clear();
            // Test auto-hide skip button:
            // Button should appear only once
            segments.add(new SponsorSegment(SegmentCategory.INTRO, "debug", 5000, 120000, false));
            // Button should appear only once
            segments.add(new SponsorSegment(SegmentCategory.SELF_PROMO, "debug", 10000, 60000, false));
            // Button should appear only once
            segments.add(new SponsorSegment(SegmentCategory.INTERACTION, "debug", 15000, 20000, false));
            // Button should appear _twice_ (at 21s and 27s)
            segments.add(new SponsorSegment(SegmentCategory.SPONSOR, "debug", 21000, 30000, false));
            // Button should appear only once
            segments.add(new SponsorSegment(SegmentCategory.OUTRO, "debug", 24000, 27000, false));


            // Test seekbar visibility:
            // All three segments should be viewable on the seekbar
            segments.add(new SponsorSegment(SegmentCategory.MUSIC_OFFTOPIC, "debug", 200000, 300000, false));
            segments.add(new SponsorSegment(SegmentCategory.SPONSOR, "debug", 200000, 250000, false));
            segments.add(new SponsorSegment(SegmentCategory.SELF_PROMO, "debug", 200000, 330000, false));
        }

        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(@NonNull String videoId, @NonNull String category,
                                      long startTime, long endTime, long videoLength) {
        ReVancedUtils.verifyOffMainThread();
        try {
            String privateUserId = SponsorBlockSettings.getSBPrivateUserID();
            String start = String.format(Locale.US, TIME_TEMPLATE, startTime / 1000f);
            String end = String.format(Locale.US, TIME_TEMPLATE, endTime / 1000f);
            String duration = String.format(Locale.US, TIME_TEMPLATE, videoLength / 1000f);

            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.SUBMIT_SEGMENTS, privateUserId, videoId, category, start, end, duration);
            final int responseCode = connection.getResponseCode();

            final String messageToToast;
            switch (responseCode) {
                case HTTP_STATUS_CODE_SUCCESS:
                    messageToToast = str("sb_submit_succeeded");
                    break;
                case 409:
                    messageToToast = str("sb_submit_failed_duplicate");
                    break;
                case 403:
                    messageToToast = str("sb_submit_failed_forbidden", Requester.parseErrorJsonAndDisconnect(connection));
                    break;
                case 429:
                    messageToToast = str("sb_submit_failed_rate_limit");
                    break;
                case 400:
                    messageToToast = str("sb_submit_failed_invalid", Requester.parseErrorJsonAndDisconnect(connection));
                    break;
                default:
                    messageToToast = str("sb_submit_failed_unknown_error", responseCode, connection.getResponseMessage());
                    break;
            }
            ReVancedUtils.showToastLong(messageToToast);
        } catch (SocketTimeoutException ex) {
            // Always show, even if show connection toasts is turned off
            ReVancedUtils.showToastLong(str("sb_submit_failed_timeout"));
        } catch (IOException ex) {
            ReVancedUtils.showToastLong(str("sb_submit_failed_unknown_error", 0, ex.getMessage()));
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to submit segments", ex);
        }
    }

    public static void sendSegmentSkippedViewedRequest(@NonNull SponsorSegment segment) {
        ReVancedUtils.verifyOffMainThread();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.VIEWED_SEGMENT, segment.UUID);
            final int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                LogHelper.printDebug(() -> "Successfully sent view count for segment: " + segment);
            } else {
                LogHelper.printDebug(() -> "Failed to sent view count for segment: " + segment.UUID
                        + " responseCode: " + responseCode); // debug level, no toast is shown
            }
        } catch (IOException ex) {
            LogHelper.printInfo(() -> "Failed to send view count", ex); // do not show a toast
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to send view count request", ex); // should never happen
        }
    }

    public static void voteForSegmentOnBackgroundThread(@NonNull SponsorSegment segment, @NonNull SegmentVote voteOption) {
        voteOrRequestCategoryChange(segment, voteOption, null);
    }
    public static void voteToChangeCategoryOnBackgroundThread(@NonNull SponsorSegment segment, @NonNull SegmentCategory categoryToVoteFor) {
        voteOrRequestCategoryChange(segment, SegmentVote.CATEGORY_CHANGE, categoryToVoteFor);
    }
    private static void voteOrRequestCategoryChange(@NonNull SponsorSegment segment, @NonNull SegmentVote voteOption, SegmentCategory categoryToVoteFor) {
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                String segmentUuid = segment.UUID;
                String uuid = SponsorBlockSettings.getSBPrivateUserID();
                HttpURLConnection connection = (voteOption == SegmentVote.CATEGORY_CHANGE)
                        ? getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_CATEGORY, uuid, segmentUuid, categoryToVoteFor.key)
                        : getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_QUALITY, uuid, segmentUuid, String.valueOf(voteOption.apiVoteType));
                final int responseCode = connection.getResponseCode();

                switch (responseCode) {
                    case HTTP_STATUS_CODE_SUCCESS:
                        LogHelper.printDebug(() -> "Vote success for segment: " + segment);
                        break;
                    case 403:
                        ReVancedUtils.showToastLong(
                                str("sb_vote_failed_forbidden", Requester.parseErrorJsonAndDisconnect(connection)));
                        break;
                    default:
                        ReVancedUtils.showToastLong(
                                str("sb_vote_failed_unknown_error", responseCode, connection.getResponseMessage()));
                        break;
                }
            } catch (SocketTimeoutException ex) {
                ReVancedUtils.showToastShort(str("sb_vote_failed_timeout"));
            } catch (IOException ex) {
                ReVancedUtils.showToastShort(str("sb_vote_failed_unknown_error", 0, ex.getMessage()));
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to vote for segment", ex); // should never happen
            }
        });
    }

    /**
     * @return NULL, if stats fetch failed
     */
    @Nullable
    public static UserStats retrieveUserStats() {
        ReVancedUtils.verifyOffMainThread();
        try {
            UserStats stats = new UserStats(getJSONObject(SBRoutes.GET_USER_STATS, SponsorBlockSettings.getSBPrivateUserID()));
            LogHelper.printDebug(() -> "user stats: " + stats);
            return stats;
        } catch (IOException ex) {
            LogHelper.printInfo(() -> "failed to retrieve user stats", ex); // info level, do not show a toast
        } catch (Exception ex) {
            LogHelper.printException(() -> "failure retrieving user stats", ex); // should never happen
        }
        return null;
    }

    /**
     * @return NULL if the call was successful.  If unsuccessful, an error message is returned.
     */
    @Nullable
    public static String setUsername(@NonNull String username) {
        ReVancedUtils.verifyOffMainThread();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.CHANGE_USERNAME, SponsorBlockSettings.getSBPrivateUserID(), username);
            final int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                return null;
            }
            return str("sb_stats_username_change_unknown_error", responseCode, responseMessage);
        } catch (Exception ex) { // should never happen
            LogHelper.printInfo(() -> "failed to set username", ex); // do not toast
            return str("sb_stats_username_change_unknown_error", 0, ex.getMessage());
        }
    }

    public static void runVipCheckInBackgroundIfNeeded() {
        if (!SponsorBlockSettings.userHasSBPrivateId()) {
            return; // User cannot be a VIP. User has never voted, created any segments, or has imported a SB user id.
        }
        long now = System.currentTimeMillis();
        if (now < (SettingsEnum.SB_LAST_VIP_CHECK.getLong() + TimeUnit.DAYS.toMillis(3))) {
            return;
        }
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                JSONObject json = getJSONObject(SBRoutes.IS_USER_VIP, SponsorBlockSettings.getSBPrivateUserID());
                boolean vip = json.getBoolean("vip");
                SettingsEnum.SB_USER_IS_VIP.saveValue(vip);
                SettingsEnum.SB_LAST_VIP_CHECK.saveValue(now);
            } catch (IOException ex) {
                LogHelper.printInfo(() -> "Failed to check VIP (network error)", ex); // info, so no error toast is shown
            } catch (Exception ex) {
                LogHelper.printException(() -> "Failed to check VIP", ex); // should never happen
            }
        });
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(@NonNull Route route, String... params) throws IOException {
        HttpURLConnection connection = Requester.getConnectionFromRoute(SettingsEnum.SB_API_URL.getString(), route, params);
        connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
        connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);
        return connection;
    }

    private static JSONObject getJSONObject(@NonNull Route route, String... params) throws IOException, JSONException {
        return Requester.parseJSONObject(getConnectionFromRoute(route, params));
    }
}
