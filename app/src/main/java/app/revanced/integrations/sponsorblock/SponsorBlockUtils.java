package app.revanced.integrations.sponsorblock;

import static android.text.Html.fromHtml;
import static app.revanced.integrations.sponsorblock.StringRef.str;

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
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.objects.UserStats;
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public abstract class SponsorBlockUtils {
    public static final String DATE_FORMAT = "HH:mm:ss.SSS";
    @SuppressLint("SimpleDateFormat")
    // must be used exclusively on the main thread (not thread safe)
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    // must be used exclusively on the main thread (not thread safe)
    private static final DecimalFormat statsFormatter = new DecimalFormat("#,###,###");
    private static final String STATS_FORMAT_TEMPLATE = "%dh %d %s";
    private static final String LOCKED_COLOR = "#FFC83D";

    private static Context playerViewGroupContext;

    private static long newSponsorSegmentDialogShownMillis;
    private static long newSponsorSegmentStartMillis = -1;
    private static long newSponsorSegmentEndMillis = -1;
    private static boolean newSponsorSegmentPreviewed;
    private static final DialogInterface.OnClickListener newSponsorSegmentDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    // start
                    newSponsorSegmentStartMillis = newSponsorSegmentDialogShownMillis;
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    // end
                    newSponsorSegmentEndMillis = newSponsorSegmentDialogShownMillis;
                    break;
            }
            dialog.dismiss();
        }
    };
    private static SponsorBlockSettings.SegmentInfo newSponsorBlockSegmentType;
    private static final DialogInterface.OnClickListener segmentTypeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted()[which];
                boolean enableButton;
                if (!segmentType.behaviour.showOnTimeBar) {
                    ReVancedUtils.showToastLong(str("new_segment_disabled_category"));
                    enableButton = false;
                } else {
                    ReVancedUtils.showToastShort(segmentType.description.toString());
                    newSponsorBlockSegmentType = segmentType;
                    enableButton = true;
                }

                ((AlertDialog) dialog)
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .setEnabled(enableButton);
            } catch (Exception ex) {
                LogHelper.printException(() -> "segmentTypeListener failure", ex);
            }
        }
    };
    private static final DialogInterface.OnClickListener segmentReadyDialogButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                SponsorBlockView.hideNewSegmentLayout();
                Context context = ((AlertDialog) dialog).getContext();
                dialog.dismiss();

                SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
                CharSequence[] titles = new CharSequence[values.length];
                for (int i = 0; i < values.length; i++) {
                    // titles[i] = values[i].title;
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
            } catch (Exception ex) {
                LogHelper.printException(() -> "segmentReadyDialogButtonListener failure", ex);
            }
        }
    };
    private static final DialogInterface.OnClickListener segmentCategorySelectedDialogListener = (dialog, which) -> {
        dialog.dismiss();
        submitNewSegment();
    };
    private static final EditByHandSaveDialogListener editByHandSaveDialogListener = new EditByHandSaveDialogListener();
    private static final DialogInterface.OnClickListener editByHandDialogListener = (dialog, which) -> {
        try {
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
        } catch (Exception ex) {
            LogHelper.printException(() -> "editByHandDialogListener failure", ex);
        }
    };
    private static final DialogInterface.OnClickListener segmentVoteClickListener = (dialog, which) -> {
        try {
            final Context context = ((AlertDialog) dialog).getContext();
            SponsorSegment[] currentSegments = PlayerController.getSegmentsOfCurrentVideo();
            if (currentSegments == null || currentSegments.length == 0) {
                LogHelper.printException(() -> "Segment is no longer available on the client");
                return;
            }
            SponsorSegment segment = currentSegments[which];

            final VoteOption[] voteOptions = VoteOption.values();
            CharSequence[] items = new CharSequence[voteOptions.length];

            for (int i = 0; i < voteOptions.length; i++) {
                VoteOption voteOption = voteOptions[i];
                String title = voteOption.title;
                if (SettingsEnum.SB_IS_VIP.getBoolean() && segment.isLocked && voteOption.shouldHighlight) {
                    items[i] = Html.fromHtml(String.format("<font color=\"%s\">%s</font>", LOCKED_COLOR, title));
                } else {
                    items[i] = title;
                }
            }

            new AlertDialog.Builder(context)
                    .setItems(items, (dialog1, which1) -> {
                        VoteOption voteOption = voteOptions[which1];
                        switch (voteOption) {
                            case UPVOTE:
                            case DOWNVOTE:
                                SBRequester.voteForSegmentOnBackgroundThread(segment, voteOption);
                                break;
                            case CATEGORY_CHANGE:
                                onNewCategorySelect(segment, context);
                                break;
                        }
                    })
                    .show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPreviewClicked failure", ex);
        }
    };

    static void setNewSponsorSegmentPreviewed() {
        newSponsorSegmentPreviewed = true;
    }

    private static void submitNewSegment() {
        try {
            ReVancedUtils.verifyOnMainThread();
            final String uuid = SettingsEnum.SB_UUID.getString();
            final long start = newSponsorSegmentStartMillis;
            final long end = newSponsorSegmentEndMillis;
            final String videoId = PlayerController.getCurrentVideoId();
            final long videoLength = VideoInformation.getCurrentVideoLength();
            final SponsorBlockSettings.SegmentInfo segmentType = newSponsorBlockSegmentType;
            if (start < 0 || end < 0 || start >= end || segmentType == null || videoId == null || uuid == null) {
                LogHelper.printException(() -> "Unable to submit times, invalid parameters");
                return;
            }
            newSponsorSegmentEndMillis = newSponsorSegmentStartMillis = -1;
            newSponsorSegmentPreviewed = false;
            ReVancedUtils.runOnBackgroundThread(() -> {
                SBRequester.submitSegments(uuid, videoId, segmentType.key, start, end, videoLength);
                PlayerController.executeDownloadSegments(videoId);
            });
        } catch (Exception e) {
            LogHelper.printException(() -> "Unable to submit segment", e);
        }
    }

    private SponsorBlockUtils() {
    }

    static void setPlayerViewGroupContext(Context context) {
        playerViewGroupContext = Objects.requireNonNull(context);
    }

    public static void onMarkLocationClicked() {
        try {
            ReVancedUtils.verifyOnMainThread();
            newSponsorSegmentDialogShownMillis = VideoInformation.getVideoTime();

            new AlertDialog.Builder(playerViewGroupContext)
                    .setTitle(str("new_segment_title"))
                    .setMessage(str("new_segment_mark_time_as_question",
                            newSponsorSegmentDialogShownMillis / 60000,
                            newSponsorSegmentDialogShownMillis / 1000 % 60,
                            newSponsorSegmentDialogShownMillis % 1000))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("new_segment_mark_start"), newSponsorSegmentDialogListener)
                    .setPositiveButton(str("new_segment_mark_end"), newSponsorSegmentDialogListener)
                    .show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "onMarkLocationClicked failure", ex);
        }
    }

    public static void onPublishClicked() {
        try {
            ReVancedUtils.verifyOnMainThread();
            if (!newSponsorSegmentPreviewed) {
                ReVancedUtils.showToastLong(str("new_segment_preview_segment_first"));
            } else if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
                long length = (newSponsorSegmentEndMillis - newSponsorSegmentStartMillis) / 1000;
                long start = (newSponsorSegmentStartMillis) / 1000;
                long end = (newSponsorSegmentEndMillis) / 1000;
                new AlertDialog.Builder(playerViewGroupContext)
                        .setTitle(str("new_segment_confirm_title"))
                        .setMessage(str("new_segment_confirm_content",
                                start / 60, start % 60,
                                end / 60, end % 60,
                                length / 60, length % 60))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, segmentReadyDialogButtonListener)
                        .show();
            } else {
                ReVancedUtils.showToastShort(str("new_segment_mark_locations_first"));
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPublishClicked failure", ex);
        }
    }

    public static void onVotingClicked(final Context context) {
        try {
            ReVancedUtils.verifyOnMainThread();
            SponsorSegment[] currentSegments = PlayerController.getSegmentsOfCurrentVideo();
            if (currentSegments == null || currentSegments.length == 0) {
                ReVancedUtils.showToastShort(str("vote_no_segments"));
                return;
            }
            int segmentAmount = currentSegments.length;
            List<CharSequence> titles = new ArrayList<>(segmentAmount); // I've replaced an array with a list to prevent null elements in the array as unsubmitted segments get filtered out
            for (int i = 0; i < segmentAmount; i++) {
                SponsorSegment segment = currentSegments[i];
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
        } catch (Exception ex) {
            LogHelper.printException(() -> "onVotingClicked failure", ex);
        }
    }

    private static void onNewCategorySelect(final SponsorSegment segment, Context context) {
        try {
            ReVancedUtils.verifyOnMainThread();
            final SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            CharSequence[] titles = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
                titles[i] = values[i].getTitleWithDot();
            }

            new AlertDialog.Builder(context)
                    .setTitle(str("new_segment_choose_category"))
                    .setItems(titles, (dialog, which) -> SBRequester.voteForSegmentOnBackgroundThread(segment, VoteOption.CATEGORY_CHANGE, values[which].key))
                    .show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "onNewCategorySelect failure", ex);
        }
    }

    public static void onPreviewClicked() {
        try {
            ReVancedUtils.verifyOnMainThread();
            if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
                VideoInformation.seekTo(newSponsorSegmentStartMillis - 2500);
                final SponsorSegment[] original = PlayerController.getSegmentsOfCurrentVideo();
                final SponsorSegment[] segments = original == null ? new SponsorSegment[1] : Arrays.copyOf(original, original.length + 1);

                segments[segments.length - 1] = new SponsorSegment(SponsorBlockSettings.SegmentInfo.UNSUBMITTED, null,
                        newSponsorSegmentStartMillis, newSponsorSegmentEndMillis, false);

                PlayerController.setSegmentsOfCurrentVideo(segments);
            } else {
                ReVancedUtils.showToastShort(str("new_segment_mark_locations_first"));
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPreviewClicked failure", ex);
        }
    }


    public static void sendViewRequestAsync(final long millis, final SponsorSegment segment) {
        if (segment.category != SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
            final long newSkippedTime = SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.getLong() + (segment.end - segment.start);
            SettingsEnum.SB_SKIPPED_SEGMENTS.saveValue(SettingsEnum.SB_SKIPPED_SEGMENTS.getInt() + 1);
            SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.saveValue(newSkippedTime);

            // maximum time a segment can be watched and still considered a 'skipped view'
            final int viewLengthThresholdToCountSkip = 2000; // count skip if user watches the segment less than 2 seconds
            if (SettingsEnum.SB_COUNT_SKIPS.getBoolean() && (millis - segment.start < viewLengthThresholdToCountSkip)) {
                ReVancedUtils.runOnBackgroundThread(() -> SBRequester.sendViewCountRequest(segment));
            }
        }
    }

    public static void onEditByHandClicked() {
        try {
            ReVancedUtils.verifyOnMainThread();
            new AlertDialog.Builder(playerViewGroupContext)
                    .setTitle(str("new_segment_edit_by_hand_title"))
                    .setMessage(str("new_segment_edit_by_hand_content"))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("new_segment_mark_start"), editByHandDialogListener)
                    .setPositiveButton(str("new_segment_mark_end"), editByHandDialogListener)
                    .show();
        } catch (Exception ex) {
            LogHelper.printException(() -> "onEditByHandClicked failure", ex);
        }
    }

    public static String formatColorString(int color) {
        return String.format("#%06X", color);
    }

    public static void addUserStats(PreferenceCategory category, Preference loadingPreference, UserStats stats) {
        ReVancedUtils.verifyOnMainThread();
        category.removePreference(loadingPreference);

        Context context = category.getContext();
        String minutesStr = str("minutes");

        {
            EditTextPreference preference = new EditTextPreference(context);
            category.addPreference(preference);
            String userName = stats.userName;
            preference.setTitle(fromHtml(str("stats_username", userName)));
            preference.setSummary(str("stats_username_change"));
            preference.setText(userName);
            preference.setOnPreferenceChangeListener((preference1, newUsername) -> {
                SBRequester.setUsername((String) newUsername, preference);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            String formatted = statsFormatter.format(stats.segmentCount);
            preference.setTitle(fromHtml(str("stats_submissions", formatted)));
            if (stats.segmentCount == 0) {
                preference.setSelectable(false);
            } else {
                preference.setOnPreferenceClickListener(preference1 -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://sb.ltn.fi/userid/" + stats.publicUserId));
                    preference1.getContext().startActivity(i);
                    return true;
                });
            }
        }

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);

            String stats_saved;
            String stats_saved_sum;
            if (stats.segmentCount == 0) {
                stats_saved = str("stats_saved_zero");
                stats_saved_sum = str("stats_saved_sum_zero");
            } else {
                stats_saved = str("stats_saved", statsFormatter.format(stats.viewCount));
                final double totalSaved = stats.minutesSaved;
                final int hoursSaved = (int) (totalSaved / 60);
                final int minutesSaved = (int) (totalSaved % 60);
                String formattedSaved = String.format(STATS_FORMAT_TEMPLATE, hoursSaved, minutesSaved, minutesStr);
                stats_saved_sum = str("stats_saved_sum", formattedSaved);
            }
            preference.setTitle(fromHtml(stats_saved));
            preference.setSummary(fromHtml(stats_saved_sum));
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

            Runnable updateStatsSelfSaved = () -> {
                final long totalSkippedTime = SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.getLong();
                final int hoursSaved = (int) (totalSkippedTime / (60 * 60 * 1000));
                final int minutesSaved = (int) ((totalSkippedTime / (60 * 1000)) % 60);
                String formatted = statsFormatter.format(SettingsEnum.SB_SKIPPED_SEGMENTS.getInt());
                preference.setTitle(fromHtml(str("stats_self_saved", formatted)));
                String formattedSaved = String.format(STATS_FORMAT_TEMPLATE, hoursSaved, minutesSaved, minutesStr);
                preference.setSummary(fromHtml(str("stats_self_saved_sum", formattedSaved)));
            };
            updateStatsSelfSaved.run();
            preference.setOnPreferenceClickListener(preference1 -> {
                new AlertDialog.Builder(preference1.getContext())
                        .setTitle(str("stats_self_saved_reset_title"))
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            SettingsEnum.SB_SKIPPED_SEGMENTS.setValue(SettingsEnum.SB_SKIPPED_SEGMENTS.getDefaultValue());
                            SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.setValue(SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.getDefaultValue());
                            updateStatsSelfSaved.run();
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            });
        }
    }

    public static void importSettings(String json) {
        ReVancedUtils.verifyOnMainThread();
        try {
            JSONObject settingsJson = new JSONObject(json);

            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");


            SharedPreferences.Editor editor = SharedPrefHelper.getPreferences(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK).edit();

            SponsorBlockSettings.SegmentInfo[] categories = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            for (SponsorBlockSettings.SegmentInfo category : categories) {
                String categoryKey = category.key;
                JSONObject categoryObject = barTypesObject.getJSONObject(categoryKey);
                String color = categoryObject.getString("color");

                editor.putString(categoryKey + SponsorBlockSettings.CATEGORY_COLOR_SUFFIX, color);
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
            editor.apply();

            SettingsEnum.SB_UUID.saveValue(settingsJson.getString("userID"));
            SettingsEnum.SB_IS_VIP.saveValue(settingsJson.getBoolean("isVip"));
            SettingsEnum.SB_API_URL.saveValue(settingsJson.getString("serverAddress"));
            SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.saveValue(!settingsJson.getBoolean("dontShowNotice"));
            SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.saveValue(settingsJson.getBoolean("showTimeWithSkips"));
            SettingsEnum.SB_MIN_DURATION.saveValue(Float.valueOf(settingsJson.getString("minDuration")));
            SettingsEnum.SB_COUNT_SKIPS.saveValue(settingsJson.getBoolean("trackViewCount"));

            ReVancedUtils.showToastLong(str("settings_import_successful"));
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to import settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("settings_import_failed"));
        }
    }

    public static String exportSettings() {
        ReVancedUtils.verifyOnMainThread();
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
            json.put("userID", SettingsEnum.SB_UUID.getString());
            json.put("isVip", SettingsEnum.SB_IS_VIP.getBoolean());
            json.put("serverAddress", SettingsEnum.SB_API_URL.getString());
            json.put("dontShowNotice", !SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean());
            json.put("showTimeWithSkips", SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean());
            json.put("minDuration", SettingsEnum.SB_MIN_DURATION.getFloat());
            json.put("trackViewCount", SettingsEnum.SB_COUNT_SKIPS.getBoolean());
            json.put("categorySelections", categorySelectionsArray);
            json.put("barTypes", barTypesObject);

            return json.toString();
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to export settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("settings_export_failed"));
            return "";
        }
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

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                final EditText editText = this.editText.get();
                if (editText == null) return;

                long time = (which == DialogInterface.BUTTON_NEUTRAL) ?
                        VideoInformation.getVideoTime() :
                        (Objects.requireNonNull(dateFormatter.parse(editText.getText().toString())).getTime());

                if (settingStart)
                    newSponsorSegmentStartMillis = Math.max(time, 0);
                else
                    newSponsorSegmentEndMillis = time;

                if (which == DialogInterface.BUTTON_NEUTRAL)
                    editByHandDialogListener.onClick(dialog, settingStart ?
                            DialogInterface.BUTTON_NEGATIVE :
                            DialogInterface.BUTTON_POSITIVE);
            } catch (ParseException e) {
                ReVancedUtils.showToastLong(str("new_segment_edit_by_hand_parse_error"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "EditByHandSaveDialogListener failure", ex);
            }
        }
    }
}
