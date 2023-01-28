package app.revanced.integrations.sponsorblock.requests;

import static android.text.Html.fromHtml;
import static app.revanced.integrations.sponsorblock.StringRef.str;
import static app.revanced.integrations.utils.ReVancedUtils.runOnMainThread;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.PlayerController;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils.VoteOption;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.objects.UserStats;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SBRequester {
    private static final String TIME_TEMPLATE = "%.3f";

    /**
     * TCP timeout
     */
    private static final int TIMEOUT_TCP_DEFAULT_MILLISECONDS = 10000;

    /**
     * HTTP response timeout
     */
    private static final int TIMEOUT_HTTP_DEFAULT_MILLISECONDS = 15000;

    /**
     * Response code of a successful API call
     */
    private static final int SUCCESS_HTTP_STATUS_CODE = 200;

    private SBRequester() {
    }

    public static SponsorSegment[] getSegments(String videoId) {
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.GET_SEGMENTS, videoId, SponsorBlockSettings.sponsorBlockUrlCategories);
            connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
            connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);

            final int responseCode = connection.getResponseCode();

            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                JSONArray responseArray = Requester.parseJSONArray(connection);
                for (int i = 0, length = responseArray.length(); i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    long start = (long) (segment.getDouble(0) * 1000);
                    long end = (long) (segment.getDouble(1) * 1000);

                    long minDuration = (long) (SettingsEnum.SB_MIN_DURATION.getFloat() * 1000);
                    if ((end - start) < minDuration)
                        continue;

                    String category = obj.getString("category");
                    String uuid = obj.getString("UUID");
                    boolean locked = obj.getInt("locked") == 1;

                    SponsorBlockSettings.SegmentInfo segmentCategory = SponsorBlockSettings.SegmentInfo.byCategoryKey(category);
                    if (segmentCategory != null && segmentCategory.behaviour.showOnTimeBar) {
                        SponsorSegment sponsorSegment = new SponsorSegment(start, end, segmentCategory, uuid, locked);
                        segments.add(sponsorSegment);
                    }
                }
                if (!segments.isEmpty()) {
                    SponsorBlockUtils.videoHasSegments = true;
                    SponsorBlockUtils.timeWithoutSegments = SponsorBlockUtils.getTimeWithoutSegments(segments.toArray(new SponsorSegment[0]));
                }
                runVipCheckInBackgroundIfNeeded();
            } else if (responseCode == 404) {
                // no segments are found.  a normal response
                LogHelper.printException(() -> "no segments found for video: " + videoId);
            } else {
                LogHelper.printException(() -> "getSegments failed with response code: " + responseCode,
                        null, str("sponsorblock_connection_timeout"));
                connection.disconnect(); // something went wrong, might as well disconnect
            }
        } catch (IOException ex) {
            LogHelper.printException(() -> "failed to get segments", ex, str("sponsorblock_connection_timeout"));
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to get segments", ex);
        }
        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(String videoId, String uuid, float startTime, float endTime, String category, Runnable toastRunnable) {
        try {
            String start = String.format(Locale.US, TIME_TEMPLATE, startTime);
            String end = String.format(Locale.US, TIME_TEMPLATE, endTime);
            String duration = String.valueOf(PlayerController.getCurrentVideoLength() / 1000);
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.SUBMIT_SEGMENTS, videoId, uuid, start, end, category, duration);
            connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
            connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case SUCCESS_HTTP_STATUS_CODE:
                    SponsorBlockUtils.messageToToast = str("submit_succeeded");
                    break;
                case 409:
                    SponsorBlockUtils.messageToToast = str("submit_failed_duplicate");
                    break;
                case 403:
                    SponsorBlockUtils.messageToToast = str("submit_failed_forbidden", Requester.parseErrorJsonAndDisconnect(connection));
                    break;
                case 429:
                    SponsorBlockUtils.messageToToast = str("submit_failed_rate_limit");
                    break;
                case 400:
                    SponsorBlockUtils.messageToToast = str("submit_failed_invalid", Requester.parseErrorJsonAndDisconnect(connection));
                    break;
                default:
                    SponsorBlockUtils.messageToToast = str("submit_failed_unknown_error", responseCode, connection.getResponseMessage());
                    break;
            }
            runOnMainThread(toastRunnable);
        } catch (IOException ex) {
            SponsorBlockUtils.messageToToast = str("submit_failed_timeout", ex.getMessage());
            runOnMainThread(toastRunnable);
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to submit segments", ex);
        }
    }

    public static void sendViewCountRequest(SponsorSegment segment) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.VIEWED_SEGMENT, segment.UUID);
            connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
            connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);
            final int responseCode = connection.getResponseCode();

            if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                LogHelper.printDebug(() -> "successfully sent view count for segment: " + segment.UUID);
            } else {
                LogHelper.printDebug(() -> "failed to sent view count for segment: " + segment.UUID
                        + " responseCode: " + responseCode); // debug level, no toast is shown
            }
        } catch (IOException ex) {
            LogHelper.printDebug(() -> "could not send view count: " + ex); // do not show a toast
        } catch (Exception ex) {
            LogHelper.printException(() -> "failed to send view count request", ex); // should never happen
        }
    }

    public static void voteForSegment(SponsorSegment segment, VoteOption voteOption, Context context, String... args) {
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                String segmentUuid = segment.UUID;
                String uuid = SettingsEnum.SB_UUID.getString();
                String vote = Integer.toString(voteOption == VoteOption.UPVOTE ? 1 : 0);

                HttpURLConnection connection = voteOption == VoteOption.CATEGORY_CHANGE
                        ? getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_CATEGORY, segmentUuid, uuid, args[0])
                        : getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_QUALITY, segmentUuid, uuid, vote);
                connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
                connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);

                final int responseCode = connection.getResponseCode();

                switch (responseCode) {
                    case SUCCESS_HTTP_STATUS_CODE:
                        SponsorBlockUtils.messageToToast = str("vote_succeeded");
                        break;
                    case 403:
                        SponsorBlockUtils.messageToToast = str("vote_failed_forbidden", Requester.parseErrorJsonAndDisconnect(connection));
                        break;
                    default:
                        SponsorBlockUtils.messageToToast = str("vote_failed_unknown_error", responseCode, connection.getResponseMessage());
                        break;
                }
                runOnMainThread(() -> Toast.makeText(context, SponsorBlockUtils.messageToToast, Toast.LENGTH_LONG).show());
            } catch (IOException ex) {
                SponsorBlockUtils.messageToToast = str("vote_failed_timeout", ex.getMessage());
                runOnMainThread(() -> Toast.makeText(context, SponsorBlockUtils.messageToToast, Toast.LENGTH_LONG).show());
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to vote for segment", ex);
            }
        });
    }

    public static void retrieveUserStats(PreferenceCategory category, Preference loadingPreference) {
        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            loadingPreference.setTitle(str("stats_sb_disabled"));
            return;
        }

        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                JSONObject json = getJSONObject(SBRoutes.GET_USER_STATS, SettingsEnum.SB_UUID.getString());
                UserStats stats = new UserStats(json.getString("userName"), json.getDouble("minutesSaved"), json.getInt("segmentCount"),
                        json.getInt("viewCount"));
                runOnMainThread(() -> { // get back on main thread to modify UI elements
                    SponsorBlockUtils.addUserStats(category, loadingPreference, stats);
                });
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to retrieve user stats", ex);
            }
        });
    }

    public static void setUsername(String username, EditTextPreference preference, Runnable toastRunnable) {
        ReVancedUtils.runOnBackgroundThread(() -> {
            try {
                HttpURLConnection connection = getConnectionFromRoute(SBRoutes.CHANGE_USERNAME, SettingsEnum.SB_UUID.getString(), username);
                connection.setConnectTimeout(TIMEOUT_TCP_DEFAULT_MILLISECONDS);
                connection.setReadTimeout(TIMEOUT_HTTP_DEFAULT_MILLISECONDS);
                int responseCode = connection.getResponseCode();

                if (responseCode == SUCCESS_HTTP_STATUS_CODE) {
                    SponsorBlockUtils.messageToToast = str("stats_username_changed");
                    runOnMainThread(() -> {
                        preference.setTitle(fromHtml(str("stats_username", username)));
                        preference.setText(username);
                    });
                } else {
                    SponsorBlockUtils.messageToToast = str("stats_username_change_unknown_error", responseCode, connection.getResponseMessage());
                }
                runOnMainThread(toastRunnable);
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
                LogHelper.printInfo(() -> "failed to check VIP (network error)", ex); // info, so no error toast is shown
            } catch (Exception ex) {
                LogHelper.printException(() -> "failed to check VIP", ex); // should never happen
            }
        });
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(SettingsEnum.SB_API_URL.getString(), route, params);
    }

    private static JSONObject getJSONObject(Route route, String... params) throws IOException, JSONException {
        return Requester.parseJSONObject(getConnectionFromRoute(route, params));
    }
}
