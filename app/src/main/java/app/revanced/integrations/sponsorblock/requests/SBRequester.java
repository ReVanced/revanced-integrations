package app.revanced.integrations.sponsorblock.requests;

import static android.text.Html.fromHtml;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.sponsorblock.objects.CategoryBehaviour;

import app.revanced.integrations.sponsorblock.objects.SegmentCategory;
import static app.revanced.integrations.sponsorblock.StringRef.str;
import static app.revanced.integrations.utils.ReVancedUtils.runOnMainThread;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import androidx.annotation.NonNull;

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
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
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
    private static final int SUCCESS_HTTP_STATUS_CODE = 200;

    private SBRequester() {
    }

    @NonNull
    public static SponsorSegment[] getSegments(@NonNull String videoId) {
        ReVancedUtils.verifyOffMainThread();
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.GET_SEGMENTS, videoId, SponsorBlockSettings.sponsorBlockAPIFetchCategories);
            final int responseCode = connection.getResponseCode();

            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                JSONArray responseArray = Requester.parseJSONArray(connection);
                final long minSegmentDuration = (long) (SettingsEnum.SB_MIN_DURATION.getFloat() * 1000);
                for (int i = 0, length = responseArray.length(); i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    final long start = (long) (segment.getDouble(0) * 1000);
                    final long end = (long) (segment.getDouble(1) * 1000);
                    if ((end - start) < minSegmentDuration)
                        continue;

                    String categoryKey = obj.getString("category");
                    String uuid = obj.getString("UUID");
                    boolean locked = obj.getInt("locked") == 1;

                    SegmentCategory segmentCategory = SegmentCategory.byCategoryKey(categoryKey);
                    if (segmentCategory == null) {
                        LogHelper.printException(() -> "Received unknown category: " + categoryKey); // should never happen
                    } else if (segmentCategory.behaviour != CategoryBehaviour.IGNORE) {
                        SponsorSegment sponsorSegment = new SponsorSegment(segmentCategory, uuid, start, end, locked);
                        segments.add(sponsorSegment);
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
                LogHelper.printException(() -> "getSegments failed with response code: " + responseCode,
                        null, str("sb_sponsorblock_connection_failure_status", responseCode));
                connection.disconnect(); // something went wrong, might as well disconnect
            }
        } catch (SocketTimeoutException ex) {
            LogHelper.printException(() -> "Failed to get segments", ex, str("sb_sponsorblock_connection_failure_timeout"));
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to get segments", ex, str("sb_sponsorblock_connection_failure_generic"));
        }
        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(@NonNull String userPrivateId, @NonNull String videoId, @NonNull String category,
                                      long startTime, long endTime, long videoLength) {
        ReVancedUtils.verifyOffMainThread();
        try {
            String start = String.format(Locale.US, TIME_TEMPLATE, startTime / 1000f);
            String end = String.format(Locale.US, TIME_TEMPLATE, endTime / 1000f);
            String duration = String.format(Locale.US, TIME_TEMPLATE, videoLength / 1000f);

            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.SUBMIT_SEGMENTS, userPrivateId, videoId, category, start, end, duration);
            final int responseCode = connection.getResponseCode();

            final String messageToToast;
            switch (responseCode) {
                case SUCCESS_HTTP_STATUS_CODE:
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
            ReVancedUtils.showToastLong(str("sb_submit_failed_timeout"));
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to submit segments", ex);
        }
    }

    public static void sendViewCountRequest(@NonNull SponsorSegment segment) {
        ReVancedUtils.verifyOffMainThread();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.VIEWED_SEGMENT, segment.UUID);
            final int responseCode = connection.getResponseCode();

            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
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

    public static void voteForSegmentOnBackgroundThread(@NonNull SponsorSegment segment, @NonNull SponsorSegment.SegmentVote voteOption, String... args) {
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                String segmentUuid = segment.UUID;
                String uuid = SettingsEnum.SB_UUID.getString();
                String vote = Integer.toString(voteOption == SponsorSegment.SegmentVote.UPVOTE ? 1 : 0);

                HttpURLConnection connection = voteOption == SponsorSegment.SegmentVote.CATEGORY_CHANGE
                        ? getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_CATEGORY, uuid, segmentUuid, args[0])
                        : getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_QUALITY, uuid, segmentUuid, vote);
                final int responseCode = connection.getResponseCode();

                switch (responseCode) {
                    case SUCCESS_HTTP_STATUS_CODE:
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
                LogHelper.printException(() -> "failed to vote for segment", ex, str("sb_vote_failed_timeout"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to vote for segment", ex); // should never happen
            }
        });
    }

    /**
     * Must be called off the main thread.
     */
    public static void retrieveUserStats(@NonNull PreferenceCategory category, @NonNull Preference loadingPreference) {
        ReVancedUtils.verifyOffMainThread();
        try {
            JSONObject json = getJSONObject(SBRoutes.GET_USER_STATS, SettingsEnum.SB_UUID.getString());
            UserStats stats = new UserStats(json);
            LogHelper.printDebug(() -> "user stats: " + stats);
            runOnMainThread(() -> { // get back on main thread to modify UI elements
                SponsorBlockUtils.addUserStats(category, loadingPreference, stats);
            });
        } catch (IOException ex) {
            runOnMainThread(() -> loadingPreference.setTitle(str("sb_stats_connection_failure")));
            LogHelper.printInfo(() -> "failed to retrieve user stats", ex); // info, to not show a toast
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to retrieve user stats", ex); // should never happen
        }
    }

    public static void setUsername(@NonNull String username, @NonNull EditTextPreference preference) {
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                HttpURLConnection connection = getConnectionFromRoute(SBRoutes.CHANGE_USERNAME, SettingsEnum.SB_UUID.getString(), username);
                final int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                runOnMainThread(() -> {
                    if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                        ReVancedUtils.showToastLong(str("sb_stats_username_changed"));
                        preference.setTitle(fromHtml(str("sb_stats_username", username)));
                        preference.setText(username);
                    } else {
                        ReVancedUtils.showToastLong(str("sb_stats_username_change_unknown_error", responseCode, responseMessage));
                    }
                });
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to set username", ex);
            }
        });
    }

    public static void runVipCheckInBackgroundIfNeeded() {
        long now = System.currentTimeMillis();
        if (now < (SettingsEnum.SB_LAST_VIP_CHECK.getLong() + TimeUnit.DAYS.toMillis(3))) {
            return;
        }
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                JSONObject json = getJSONObject(SBRoutes.IS_USER_VIP, SettingsEnum.SB_UUID.getString());
                boolean vip = json.getBoolean("vip");
                SettingsEnum.SB_IS_VIP.saveValue(vip);
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
