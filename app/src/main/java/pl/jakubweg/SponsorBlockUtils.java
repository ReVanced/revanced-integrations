package pl.jakubweg;

import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static fi.razerman.youtube.XGlobals.debug;
import static pl.jakubweg.PlayerController.getCurrentVideoId;
import static pl.jakubweg.PlayerController.getCurrentVideoLength;
import static pl.jakubweg.PlayerController.getLastKnownVideoTime;
import static pl.jakubweg.PlayerController.sponsorSegmentsOfCurrentVideo;
import static pl.jakubweg.SponsorBlockPreferenceFragment.FORMATTER;
import static pl.jakubweg.SponsorBlockPreferenceFragment.SAVED_TEMPLATE;
import static pl.jakubweg.SponsorBlockSettings.DEFAULT_API_URL;
import static pl.jakubweg.SponsorBlockSettings.DEFAULT_SERVER_URL;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_API_URL;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_CATEGORY_COLOR_SUFFIX;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_COUNT_SKIPS;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_IS_VIP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_LAST_VIP_CHECK;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_MIN_DURATION;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_UUID;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_NAME;
import static pl.jakubweg.SponsorBlockSettings.apiUrl;
import static pl.jakubweg.SponsorBlockSettings.countSkips;
import static pl.jakubweg.SponsorBlockSettings.getPreferences;
import static pl.jakubweg.SponsorBlockSettings.isSponsorBlockEnabled;
import static pl.jakubweg.SponsorBlockSettings.lastVipCheck;
import static pl.jakubweg.SponsorBlockSettings.minDuration;
import static pl.jakubweg.SponsorBlockSettings.showTimeWithoutSegments;
import static pl.jakubweg.SponsorBlockSettings.showToastWhenSkippedAutomatically;
import static pl.jakubweg.SponsorBlockSettings.skippedSegments;
import static pl.jakubweg.SponsorBlockSettings.skippedTime;
import static pl.jakubweg.SponsorBlockSettings.uuid;
import static pl.jakubweg.SponsorBlockSettings.vip;
import static pl.jakubweg.StringRef.str;
import static pl.jakubweg.requests.SBRequester.voteForSegment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import fi.vanced.utils.SharedPrefUtils;
import pl.jakubweg.objects.SponsorSegment;
import pl.jakubweg.objects.UserStats;
import pl.jakubweg.requests.SBRequester;

@SuppressWarnings({"LongLogTag"})
public abstract class SponsorBlockUtils {
    public static final String TAG = "jakubweg.SponsorBlockUtils";
    public static final String DATE_FORMAT = "HH:mm:ss.SSS";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
    public static boolean videoHasSegments = false;
    public static String timeWithoutSegments = "";
    private static final int sponsorBtnId = 1234;
    private static final String LOCKED_COLOR = "#FFC83D";
    public static final View.OnClickListener sponsorBlockBtnListener = v -> {
        if (debug) {
            Log.d(TAG, "Shield button clicked");
        }
        NewSegmentHelperLayout.toggle();
    };
    public static final View.OnClickListener voteButtonListener = v -> {
        if (debug) {
            Log.d(TAG, "Vote button clicked");
        }
        SponsorBlockUtils.onVotingClicked(v.getContext());
    };
    private static int shareBtnId = -1;
    private static long newSponsorSegmentDialogShownMillis;
    private static long newSponsorSegmentStartMillis = -1;
    private static long newSponsorSegmentEndMillis = -1;
    private static final DialogInterface.OnClickListener newSponsorSegmentDialogListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context context = ((AlertDialog) dialog).getContext();
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    // start
                    newSponsorSegmentStartMillis = newSponsorSegmentDialogShownMillis;
                    Toast.makeText(context.getApplicationContext(), str("new_segment_time_start_set"), Toast.LENGTH_LONG).show();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    // end
                    newSponsorSegmentEndMillis = newSponsorSegmentDialogShownMillis;
                    Toast.makeText(context.getApplicationContext(), str("new_segment_time_end_set"), Toast.LENGTH_SHORT).show();
                    break;
            }
            dialog.dismiss();
        }
    };
    private static SponsorBlockSettings.SegmentInfo newSponsorBlockSegmentType;
    private static final DialogInterface.OnClickListener segmentTypeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted()[which];
            boolean enableButton;
            if (!segmentType.behaviour.showOnTimeBar) {
                Toast.makeText(
                        ((AlertDialog) dialog).getContext().getApplicationContext(),
                        str("new_segment_disabled_category"),
                        Toast.LENGTH_SHORT).show();
                enableButton = false;
            } else {
                Toast.makeText(
                        ((AlertDialog) dialog).getContext().getApplicationContext(),
                        segmentType.description.toString(),
                        Toast.LENGTH_SHORT).show();
                newSponsorBlockSegmentType = segmentType;
                enableButton = true;
            }

            ((AlertDialog) dialog)
                    .getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(enableButton);
        }
    };
    private static final DialogInterface.OnClickListener segmentReadyDialogButtonListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            NewSegmentHelperLayout.hide();
            Context context = ((AlertDialog) dialog).getContext();
            dialog.dismiss();

            SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            CharSequence[] titles = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
//                titles[i] = values[i].title;
                titles[i] = values[i].getTitleWithDot();
            }

            newSponsorBlockSegmentType = null;
            new AlertDialog.Builder(context)
                    .setTitle(str("new_segment_choose_category"))
                    .setSingleChoiceItems(titles, -1, segmentTypeListener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, segmentCategorySelectedDialogListener)
                    .show()
                    .getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }
    };
    private static WeakReference<Context> appContext = new WeakReference<>(null);
    private static final DialogInterface.OnClickListener segmentCategorySelectedDialogListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Context context = ((AlertDialog) dialog).getContext().getApplicationContext();
            Toast.makeText(context, str("submit_started"), Toast.LENGTH_SHORT).show();

            appContext = new WeakReference<>(context);
            new Thread(submitRunnable).start();
        }
    };
    public static String messageToToast = "";
    private static final EditByHandSaveDialogListener editByHandSaveDialogListener = new EditByHandSaveDialogListener();
    private static final DialogInterface.OnClickListener editByHandDialogListener = (dialog, which) -> {
        Context context = ((AlertDialog) dialog).getContext();

        final boolean isStart = DialogInterface.BUTTON_NEGATIVE == which;

        final EditText textView = new EditText(context);
        textView.setHint(DATE_FORMAT);
        if (isStart) {
            if (newSponsorSegmentStartMillis >= 0)
                textView.setText(dateFormatter.format(new Date(newSponsorSegmentStartMillis)));
        } else {
            if (newSponsorSegmentEndMillis >= 0)
                textView.setText(dateFormatter.format(new Date(newSponsorSegmentEndMillis)));
        }

        editByHandSaveDialogListener.settingStart = isStart;
        editByHandSaveDialogListener.editText = new WeakReference<>(textView);
        new AlertDialog.Builder(context)
                .setTitle(str(isStart ? "new_segment_time_start" : "new_segment_time_end"))
                .setView(textView)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(str("new_segment_now"), editByHandSaveDialogListener)
                .setPositiveButton(android.R.string.ok, editByHandSaveDialogListener)
                .show();

        dialog.dismiss();
    };
    private static final Runnable toastRunnable = () -> {
        Context context = appContext.get();
        if (context != null && messageToToast != null)
            Toast.makeText(context, messageToToast, Toast.LENGTH_LONG).show();
    };
    private static final DialogInterface.OnClickListener segmentVoteClickListener = (dialog, which) -> {
        final Context context = ((AlertDialog) dialog).getContext();
        final SponsorSegment segment = sponsorSegmentsOfCurrentVideo[which];

        final VoteOption[] voteOptions = VoteOption.values();
        CharSequence[] items = new CharSequence[voteOptions.length];

        for (int i = 0; i < voteOptions.length; i++) {
            VoteOption voteOption = voteOptions[i];
            String title = voteOption.title;
            if (vip && segment.isLocked && voteOption.shouldHighlight) {
                items[i] = Html.fromHtml(String.format("<font color=\"%s\">%s</font>", LOCKED_COLOR, title));
            }
            else {
                items[i] = title;
            }
        }

        new AlertDialog.Builder(context)
                .setItems(items, (dialog1, which1) -> {
                    appContext = new WeakReference<>(context.getApplicationContext());
                    VoteOption voteOption = voteOptions[which1];
                    switch (voteOption) {
                        case UPVOTE:
                        case DOWNVOTE:
                            voteForSegment(segment, voteOption, appContext.get());
                            break;
                        case CATEGORY_CHANGE:
                            onNewCategorySelect(segment, context);
                            break;
                    }
                })
                .show();
    };
    private static final Runnable submitRunnable = () -> {
        messageToToast = null;
        final String uuid = SponsorBlockSettings.uuid;
        final long start = newSponsorSegmentStartMillis;
        final long end = newSponsorSegmentEndMillis;
        final String videoId = getCurrentVideoId();
        final SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockUtils.newSponsorBlockSegmentType;
        try {
            if (start < 0 || end < 0 || start >= end || segmentType == null || videoId == null || uuid == null) {
                Log.e(TAG, "Unable to submit times, invalid parameters");
                return;
            }
            SBRequester.submitSegments(videoId, uuid, ((float) start) / 1000f, ((float) end) / 1000f, segmentType.key, toastRunnable);
            newSponsorSegmentEndMillis = newSponsorSegmentStartMillis = -1;
        } catch (Exception e) {
            Log.e(TAG, "Unable to submit segment", e);
        }

        if (videoId != null)
            PlayerController.executeDownloadSegments(videoId);
    };

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private SponsorBlockUtils() {
    }

    public static void showShieldButton() {
        View i = ShieldButton._shieldBtn.get();
        if (i == null || !ShieldButton.shouldBeShown()) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hideShieldButton() {
        View i = ShieldButton._shieldBtn.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    public static void showVoteButton() {
        View i = VotingButton._votingButton.get();
        if (i == null || !VotingButton.shouldBeShown()) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hideVoteButton() {
        View i = VotingButton._votingButton.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    @SuppressLint("DefaultLocale")
    public static void onMarkLocationClicked(Context context) {
        newSponsorSegmentDialogShownMillis = PlayerController.getLastKnownVideoTime();

        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_title"))
                .setMessage(str("new_segment_mark_time_as_question",
                        newSponsorSegmentDialogShownMillis / 60000,
                        newSponsorSegmentDialogShownMillis / 1000 % 60,
                        newSponsorSegmentDialogShownMillis % 1000))
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(str("new_segment_mark_start"), newSponsorSegmentDialogListener)
                .setPositiveButton(str("new_segment_mark_end"), newSponsorSegmentDialogListener)
                .show();
    }

    @SuppressLint("DefaultLocale")
    public static void onPublishClicked(Context context) {
        if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
            long length = (newSponsorSegmentEndMillis - newSponsorSegmentStartMillis) / 1000;
            long start = (newSponsorSegmentStartMillis) / 1000;
            long end = (newSponsorSegmentEndMillis) / 1000;
            new AlertDialog.Builder(context)
                    .setTitle(str("new_segment_confirm_title"))
                    .setMessage(str("new_segment_confirm_content",
                            start / 60, start % 60,
                            end / 60, end % 60,
                            length / 60, length % 60))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, segmentReadyDialogButtonListener)
                    .show();
        } else {
            Toast.makeText(context, str("new_segment_mark_locations_first"), Toast.LENGTH_SHORT).show();
        }
    }

    public static void onVotingClicked(final Context context) {
        if (sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) {
            Toast.makeText(context.getApplicationContext(), str("vote_no_segments"), Toast.LENGTH_SHORT).show();
            return;
        }
        int segmentAmount = sponsorSegmentsOfCurrentVideo.length;
        List<CharSequence> titles = new ArrayList<>(segmentAmount); // I've replaced an array with a list to prevent null elements in the array as unsubmitted segments get filtered out
        for (int i = 0; i < segmentAmount; i++) {
            SponsorSegment segment = sponsorSegmentsOfCurrentVideo[i];
            if (segment.category == SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
                continue;
            }

            String start = dateFormatter.format(new Date(segment.start));
            String end = dateFormatter.format(new Date(segment.end));
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append(String.format("<b><font color=\"#%06X\">⬤</font> %s<br> %s to %s",
                    segment.category.color, segment.category.title, start, end));
            if (i + 1 != segmentAmount) // prevents trailing new line after last segment
                htmlBuilder.append("<br>");
            titles.add(Html.fromHtml(htmlBuilder.toString()));
        }

        new AlertDialog.Builder(context)
                .setItems(titles.toArray(new CharSequence[0]), segmentVoteClickListener)
                .show();
    }

    private static void onNewCategorySelect(final SponsorSegment segment, Context context) {
        final SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
        CharSequence[] titles = new CharSequence[values.length];
        for (int i = 0; i < values.length; i++) {
            titles[i] = values[i].getTitleWithDot();
        }

        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_choose_category"))
                .setItems(titles, (dialog, which) -> voteForSegment(segment, VoteOption.CATEGORY_CHANGE, appContext.get(), values[which].key))
                .show();
    }

    @SuppressLint("DefaultLocale")
    public static void onPreviewClicked(Context context) {
        if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
//            Toast t = Toast.makeText(context, "Preview", Toast.LENGTH_SHORT);
//            t.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, t.getXOffset(), t.getYOffset());
//            t.show();
            PlayerController.skipToMillisecond(newSponsorSegmentStartMillis - 3000);
            final SponsorSegment[] original = PlayerController.sponsorSegmentsOfCurrentVideo;
            final SponsorSegment[] segments = original == null ? new SponsorSegment[1] : Arrays.copyOf(original, original.length + 1);

            segments[segments.length - 1] = new SponsorSegment(newSponsorSegmentStartMillis, newSponsorSegmentEndMillis,
                    SponsorBlockSettings.SegmentInfo.UNSUBMITTED, null, false);

            Arrays.sort(segments);
            sponsorSegmentsOfCurrentVideo = segments;
        } else {
            Toast.makeText(context, str("new_segment_mark_locations_first"), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    public static void onEditByHandClicked(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_edit_by_hand_title"))
                .setMessage(str("new_segment_edit_by_hand_content"))
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(str("new_segment_mark_start"), editByHandDialogListener)
                .setPositiveButton(str("new_segment_mark_end"), editByHandDialogListener)
                .show();
    }

    public static void notifyShareBtnVisibilityChanged(View v) {
        if (v.getId() != shareBtnId || !/*SponsorBlockSettings.isAddNewSegmentEnabled*/false) return;
//        if (VERBOSE)
//            Log.d(TAG, "VISIBILITY CHANGED of view " + v);
        ImageView sponsorBtn = ShieldButton._shieldBtn.get();
        if (sponsorBtn != null) {
            sponsorBtn.setVisibility(v.getVisibility());
        }
    }

    public static String appendTimeWithoutSegments(String totalTime) {
        if (videoHasSegments && isSettingEnabled(showTimeWithoutSegments) && !TextUtils.isEmpty(totalTime) && getCurrentVideoLength() > 1) {
            if (timeWithoutSegments.isEmpty()) {
                timeWithoutSegments = getTimeWithoutSegments(sponsorSegmentsOfCurrentVideo);
            }
            return totalTime + timeWithoutSegments;
        }

        return totalTime;
    }

    public static String getTimeWithoutSegments(SponsorSegment[] sponsorSegmentsOfCurrentVideo) {
        long currentVideoLength = getCurrentVideoLength();
        if (!isSettingEnabled(showTimeWithoutSegments) || sponsorSegmentsOfCurrentVideo == null || currentVideoLength <= 1) {
            return "";
        }
        long timeWithoutSegments = currentVideoLength + 500; // YouTube:tm:
        for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
            timeWithoutSegments -= segment.end - segment.start;
        }
        long hours = timeWithoutSegments / 3600000;
        long minutes = (timeWithoutSegments / 60000) % 60;
        long seconds = (timeWithoutSegments / 1000) % 60;
        String format = (hours > 0 ? "%d:%02" : "%") + "d:%02d"; // mmLul
        String formatted = hours > 0 ? String.format(format, hours, minutes, seconds) : String.format(format, minutes, seconds);
        return String.format(" (%s)", formatted);
    }

    public static void playerTypeChanged(String playerType) {
        try {
            if (videoHasSegments && (playerType.equalsIgnoreCase("NONE"))) {
                PlayerController.setCurrentVideoId(null);
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Player type changed caused a crash.", ex);
        }
    }

    public static String formatColorString(int color) {
        return String.format("#%06X", color);
    }

    @SuppressWarnings("deprecation")
    public static void addUserStats(PreferenceCategory category, Preference loadingPreference, UserStats stats) {
        category.removePreference(loadingPreference);

        Context context = category.getContext();
        String minutesStr = str("minutes");

        {
            EditTextPreference preference = new EditTextPreference(context);
            category.addPreference(preference);
            String userName = stats.getUserName();
            preference.setTitle(fromHtml(str("stats_username", userName)));
            preference.setSummary(str("stats_username_change"));
            preference.setText(userName);
            preference.setOnPreferenceChangeListener((preference1, newUsername) -> {
                appContext = new WeakReference<>(context.getApplicationContext());
                SBRequester.setUsername((String) newUsername, preference, toastRunnable);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            String formatted = FORMATTER.format(stats.getSegmentCount());
            preference.setTitle(fromHtml(str("stats_submissions", formatted)));
        }

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            String formatted = FORMATTER.format(stats.getViewCount());

            double saved = stats.getMinutesSaved();
            int hoursSaved = (int) (saved / 60);
            double minutesSaved = saved % 60;
            String formattedSaved = String.format(SAVED_TEMPLATE, hoursSaved, minutesSaved, minutesStr);

            preference.setTitle(fromHtml(str("stats_saved", formatted)));
            preference.setSummary(fromHtml(str("stats_saved_sum", formattedSaved)));
            preference.setOnPreferenceClickListener(preference1 -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://sponsor.ajay.app/stats/"));
                preference1.getContext().startActivity(i);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            String formatted = FORMATTER.format(skippedSegments);

            long hoursSaved = skippedTime / 3600000;
            double minutesSaved = (skippedTime / 60000d) % 60;
            String formattedSaved = String.format(SAVED_TEMPLATE, hoursSaved, minutesSaved, minutesStr);

            preference.setTitle(fromHtml(str("stats_self_saved", formatted)));
            preference.setSummary(fromHtml(str("stats_self_saved_sum", formattedSaved)));
        }
    }

    public static void importSettings(String json, Context context) {
        try {
            JSONObject settingsJson = new JSONObject(json);

            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");

            SharedPreferences.Editor editor = getPreferences(context).edit();

            SponsorBlockSettings.SegmentInfo[] categories = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            for (SponsorBlockSettings.SegmentInfo category : categories) {
                String categoryKey = category.key;
                JSONObject categoryObject = barTypesObject.getJSONObject(categoryKey);
                String color = categoryObject.getString("color");

                editor.putString(categoryKey + PREFERENCES_KEY_CATEGORY_COLOR_SUFFIX, color);
                editor.putString(categoryKey, SponsorBlockSettings.SegmentBehaviour.IGNORE.key);
            }

            for (int i = 0; i < categorySelectionsArray.length(); i++) {
                JSONObject categorySelectionObject = categorySelectionsArray.getJSONObject(i);

                String categoryKey = categorySelectionObject.getString("name");
                SponsorBlockSettings.SegmentInfo category = SponsorBlockSettings.SegmentInfo.byCategoryKey(categoryKey);

                if (category == null) {
                    continue;
                }

                int desktopKey = categorySelectionObject.getInt("option");
                SponsorBlockSettings.SegmentBehaviour behaviour = SponsorBlockSettings.SegmentBehaviour.byDesktopKey(desktopKey);
                editor.putString(category.key, behaviour.key);
            }

            editor.putBoolean(PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP, !settingsJson.getBoolean("dontShowNotice"));
            editor.putBoolean(PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS, settingsJson.getBoolean("showTimeWithSkips"));
            editor.putBoolean(PREFERENCES_KEY_COUNT_SKIPS, settingsJson.getBoolean("trackViewCount"));
            editor.putBoolean(PREFERENCES_KEY_IS_VIP, settingsJson.getBoolean("isVip"));
            editor.putString(PREFERENCES_KEY_MIN_DURATION, settingsJson.getString("minDuration"));
            editor.putString(PREFERENCES_KEY_UUID, settingsJson.getString("userID"));
            editor.putString(PREFERENCES_KEY_LAST_VIP_CHECK, settingsJson.getString("lastIsVipUpdate"));

            String serverAddress = settingsJson.getString("serverAddress");
            if (serverAddress.equalsIgnoreCase(DEFAULT_SERVER_URL)) {
                serverAddress = DEFAULT_API_URL;
            }
            editor.putString(PREFERENCES_KEY_API_URL, serverAddress);

            editor.apply();

            Toast.makeText(context, str("settings_import_successful"), Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex) {
            Toast.makeText(context, str("settings_import_failed"), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public static String exportSettings(Context context) {
        try {
            JSONObject json = new JSONObject();

            JSONObject barTypesObject = new JSONObject(); // categories' colors
            JSONArray categorySelectionsArray = new JSONArray(); // categories' behavior

            SponsorBlockSettings.SegmentInfo[] categories = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            for (SponsorBlockSettings.SegmentInfo category : categories) {
                JSONObject categoryObject = new JSONObject();
                String categoryKey = category.key;
                categoryObject.put("color", formatColorString(category.color));
                barTypesObject.put(categoryKey, categoryObject);

                int desktopKey = category.behaviour.desktopKey;
                if (desktopKey != -1) {
                    JSONObject behaviorObject = new JSONObject();
                    behaviorObject.put("name", categoryKey);
                    behaviorObject.put("option", desktopKey);
                    categorySelectionsArray.put(behaviorObject);
                }
            }
            json.put("dontShowNotice", !showToastWhenSkippedAutomatically);
            json.put("barTypes", barTypesObject);
            json.put("showTimeWithSkips", showTimeWithoutSegments);
            json.put("minDuration", minDuration);
            json.put("trackViewCount", countSkips);
            json.put("categorySelections", categorySelectionsArray);
            json.put("userID", uuid);
            json.put("isVip", vip);
            json.put("lastIsVipUpdate", lastVipCheck);

            String apiAddress = apiUrl;
            if (apiAddress.equalsIgnoreCase(DEFAULT_API_URL)) {
                apiAddress = DEFAULT_SERVER_URL;
            }
            json.put("serverAddress", apiAddress);

            return json.toString();
        }
        catch (Exception ex) {
            Toast.makeText(context, str("settings_export_failed"), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return "";
        }
    }

    public static boolean isSettingEnabled(boolean setting) {
        return isSponsorBlockEnabled && setting;
    }

    public static boolean isSBButtonEnabled(Context context, String key) {
        return isSettingEnabled(SharedPrefUtils.getBoolean(context, PREFERENCES_NAME, key, false));
    }

    public enum VoteOption {
        UPVOTE(str("vote_upvote"), false),
        DOWNVOTE(str("vote_downvote"), true),
        CATEGORY_CHANGE(str("vote_category"), true);

        public final String title;
        public final boolean shouldHighlight;


        VoteOption(String title, boolean shouldHighlight) {
            this.title = title;
            this.shouldHighlight = shouldHighlight;
        }
    }

    private static class EditByHandSaveDialogListener implements DialogInterface.OnClickListener {
        public boolean settingStart;
        public WeakReference<EditText> editText;

        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final EditText editText = this.editText.get();
            if (editText == null) return;
            Context context = ((AlertDialog) dialog).getContext();

            try {
                long time = (which == DialogInterface.BUTTON_NEUTRAL) ?
                        getLastKnownVideoTime() :
                        (Objects.requireNonNull(dateFormatter.parse(editText.getText().toString())).getTime());

                if (settingStart)
                    newSponsorSegmentStartMillis = Math.max(time, 0);
                else
                    newSponsorSegmentEndMillis = time;

                if (which == DialogInterface.BUTTON_NEUTRAL)
                    editByHandDialogListener.onClick(dialog, settingStart ?
                            DialogInterface.BUTTON_NEGATIVE :
                            DialogInterface.BUTTON_POSITIVE);
                else
                    Toast.makeText(context.getApplicationContext(), str("new_segment_edit_by_hand_saved"), Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                Toast.makeText(context.getApplicationContext(), str("new_segment_edit_by_hand_parse_error"), Toast.LENGTH_LONG).show();
            }
        }
    }
}
