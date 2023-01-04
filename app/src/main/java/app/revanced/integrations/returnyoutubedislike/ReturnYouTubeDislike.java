package app.revanced.integrations.returnyoutubedislike;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ScaleXSpan;
import android.util.DisplayMetrics;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.requests.RYDVoteData;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.ThemeHelper;

public class ReturnYouTubeDislike {
    /**
     * Maximum amount of time to block the UI from updates while waiting for network call to complete.
     * <p>
     * Must be less than 5 seconds, as per:
     * https://developer.android.com/topic/performance/vitals/anr
     */
    private static final long MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_FETCH_VOTES_TO_COMPLETE = 4000;

    /**
     * Used to send votes, one by one, in the same order the user created them
     */
    private static final ExecutorService voteSerialExecutor = Executors.newSingleThreadExecutor();

    // Must be volatile, since this is read/write from different threads
    private static volatile boolean isEnabled = SettingsEnum.RYD_ENABLED.getBoolean();

    /**
     * Used to guard {@link #currentVideoId} and {@link #voteFetchFuture},
     * as multiple threads access this class.
     */
    private static final Object videoIdLockObject = new Object();

    @GuardedBy("videoIdLockObject")
    private static String currentVideoId;

    /**
     * Stores the results of the vote api fetch, and used as a barrier to wait until fetch completes
     */
    @GuardedBy("videoIdLockObject")
    private static Future<RYDVoteData> voteFetchFuture;

    public enum Vote {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0);

        public final int value;

        Vote(int value) {
            this.value = value;
        }
    }

    private ReturnYouTubeDislike() {
    } // only static methods

    /**
     * Used to format like/dislike count.
     */
    @GuardedBy("ReturnYouTubeDislike.class") // not thread safe
    private static CompactDecimalFormat dislikeCountFormatter;

    /**
     * Used to format like/dislike count.
     */
    @GuardedBy("ReturnYouTubeDislike.class")
    private static NumberFormat dislikePercentageFormatter;

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
    }

    private static String getCurrentVideoId() {
        synchronized (videoIdLockObject) {
            return currentVideoId;
        }
    }

    private static Future<RYDVoteData> getVoteFetchFuture() {
        synchronized (videoIdLockObject) {
            return voteFetchFuture;
        }
    }

    // It is unclear if this method is always called on the main thread (since the YouTube app is the one making the call)
    // treat this as if any thread could call this method
    public static void newVideoLoaded(String videoId) {
        if (!isEnabled) return;
        try {
            Objects.requireNonNull(videoId);
            LogHelper.printDebug(() -> "New video loaded: " + videoId);

            synchronized (videoIdLockObject) {
                currentVideoId = videoId;
                // no need to wrap the call in a try/catch,
                // as any exceptions are propagated out in the later Future#Get call
                voteFetchFuture = ReVancedUtils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchVotes(videoId));
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to load new video: " + videoId, ex);
        }
    }

    /**
     * This method is sometimes called on the main thread, but it usually is called _off_ the main thread.
     * <p>
     * This method can be called multiple times for the same UI element (including after dislikes was added)
     * This code should avoid needlessly replacing the same UI element with identical versions.
     */
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            String conversionContextString = conversionContext.toString();

            final boolean isSegmentedButton;
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                isSegmentedButton = true;
            } else if (conversionContextString.contains("|dislike_button.eml|")) {
                isSegmentedButton = false;
            } else {
                return;
            }

            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            RYDVoteData votingData;
            long fetchStartTime = 0;
            try {
                Future<RYDVoteData> fetchFuture = getVoteFetchFuture();
                if (SettingsEnum.DEBUG.getBoolean() && !fetchFuture.isDone()) {
                    fetchStartTime = System.currentTimeMillis();
                }
                votingData = fetchFuture.get(MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_FETCH_VOTES_TO_COMPLETE, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LogHelper.printDebug(() -> "UI timed out waiting for fetch votes to complete");
                return;
            } finally {
                recordTimeUISpentWaitingForNetworkCall(fetchStartTime);
            }
            if (votingData == null) {
                LogHelper.printDebug(() -> "Cannot add dislike to UI (RYD data not available)");
                return;
            }

            if (updateDislike(textRef, isSegmentedButton, votingData)) {
                LogHelper.printDebug(() -> "Updated dislike span to: " + textRef.get());
            } else {
                LogHelper.printDebug(() -> "Ignoring dislike span: " + textRef.get()
                        + " that appears to already show voting data: " + votingData);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error while trying to update dislikes", ex);
        }
    }

    public static void sendVote(Vote vote) {
        if (!isEnabled) return;
        try {
            Objects.requireNonNull(vote);
            Context context = Objects.requireNonNull(ReVancedUtils.getContext());
            if (SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true)) {
                return;
            }

            // Must make a local copy of videoId, since it may change between now and when the vote thread runs
            String videoIdToVoteFor = getCurrentVideoId();

            voteSerialExecutor.execute(() -> {
                // must wrap in try/catch to properly log exceptions
                try {
                    String userId = getUserId();
                    if (userId != null) {
                        ReturnYouTubeDislikeApi.sendVote(videoIdToVoteFor, userId, vote);
                    }
                } catch (Exception ex) {
                    LogHelper.printException(() -> "Failed to send vote", ex);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error while trying to send vote", ex);
        }
    }

    /**
     * Must call off main thread, as this will make a network call if user is not yet registered
     *
     * @return ReturnYouTubeDislike user ID. If user registration has never happened
     * and the network call fails, this returns NULL
     */
    @Nullable
    private static String getUserId() {
        ReVancedUtils.verifyOffMainThread();

        String userId = SettingsEnum.RYD_USER_ID.getString();
        if (userId != null) {
            return userId;
        }

        userId = ReturnYouTubeDislikeApi.registerAsNewUser(); // blocks until network call is completed
        if (userId != null) {
            SettingsEnum.RYD_USER_ID.saveValue(userId);
        }
        return userId;
    }

    /**
     * @param isSegmentedButton if UI is using the segmented single UI component for both like and dislike
     * @return false, if the text reference already has dislike information and no changes were made.
     */
    private static boolean updateDislike(AtomicReference<Object> textRef, boolean isSegmentedButton, RYDVoteData voteData) {
        Spannable oldSpannable = (Spannable) textRef.get();
        String oldLikesString = oldSpannable.toString();
        Spannable replacementSpannable;

        // note: some locales use right to left layout (arabic, hebrew, etc),
        // and care must be taken to retain the existing RTL encoding character on the likes string
        // otherwise text will incorrectly show as left to right
        // if making changes to this code, change device settings to a RTL language and verify layout is correct

        if (!isSegmentedButton) {
            // simple replacement of 'dislike' with a number/percentage
            if (stringContainsNumber(oldLikesString)) {
                // already is a number, and was modified in a previous call to this method
                return false;
            }
            replacementSpannable = newSpannableWithDislikes(oldSpannable, voteData);
        } else {
            String leftSegmentedSeparatorString = ReVancedUtils.isRightToLeftTextLayout() ? "\u200F|   " : "|   ";

            if (oldLikesString.contains(leftSegmentedSeparatorString)) {
                return false; // dislikes was previously added
            }

            // YouTube creators can hide the like count on a video,
            // and the like count appears as a device language specific string that says 'Like'
            // check if the string contains any numbers
            if (!stringContainsNumber(oldLikesString)) {
                // likes are hidden.
                // RYD does not provide usable data for these types of videos,
                // and the API returns bogus data (zero likes and zero dislikes)
                //
                // example video: https://www.youtube.com/watch?v=UnrU5vxCHxw
                // RYD data: https://returnyoutubedislikeapi.com/votes?videoId=UnrU5vxCHxw
                //
                // discussion about this: https://github.com/Anarios/return-youtube-dislike/discussions/530

                //
                // Change the "Likes" string to show that likes and dislikes are hidden
                //
                LogHelper.printDebug(() -> "Like count is hidden by video creator. "
                        + "RYD does not provide data for videos with hidden likes.");

                String hiddenMessageString = str("revanced_ryd_video_likes_hidden_by_video_owner");
                if (hiddenMessageString.equals(oldLikesString)) {
                    return false;
                }
                replacementSpannable = newSpanUsingStylingOfAnotherSpan(oldSpannable, hiddenMessageString);
            } else {
                Spannable likesSpan = newSpanUsingStylingOfAnotherSpan(oldSpannable, oldLikesString);

                // left and middle separator
                String middleSegmentedSeparatorString = "  •  ";
                Spannable leftSeparatorSpan = newSpanUsingStylingOfAnotherSpan(oldSpannable, leftSegmentedSeparatorString);
                Spannable middleSeparatorSpan = newSpanUsingStylingOfAnotherSpan(oldSpannable, middleSegmentedSeparatorString);
                // style the separator appearance to mimic the existing layout
                final int separatorColor = ThemeHelper.isDarkTheme()
                        // it might be possible to extract these colors from the litho layout
                        // and put these colors in LithoThemePatch (but that would add a dependency for this RYD patch)
                        ? 0xFF424242  // dark gray
                        : 0xFFD9D9D9; // light gray
                addSpanStyling(leftSeparatorSpan, new ForegroundColorSpan(separatorColor));
                addSpanStyling(middleSeparatorSpan, new ForegroundColorSpan(separatorColor));
                CharacterStyle noAntiAliasingStyle = new CharacterStyle() {
                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.setAntiAlias(false); // draw without anti-aliasing, to give a sharper edge
                    }
                };
                addSpanStyling(leftSeparatorSpan, noAntiAliasingStyle);
                addSpanStyling(middleSeparatorSpan, noAntiAliasingStyle);

                Spannable dislikeSpan = newSpannableWithDislikes(oldSpannable, voteData);

                // Increase the size of the left separator, so it better matches the stock separator on the right.
                // But when using a larger font, the entire span (including the like/dislike text) becomes shifted downward.
                // To correct this, use additional spans to move the alignment back upward by a relative amount.
                setSegmentedAdjustmentValues();
                class RelativeVerticalOffsetSpan extends CharacterStyle {
                    final float relativeVerticalShiftRatio;

                    RelativeVerticalOffsetSpan(float relativeVerticalShiftRatio) {
                        this.relativeVerticalShiftRatio = relativeVerticalShiftRatio;
                    }

                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.baselineShift -= (int) (relativeVerticalShiftRatio * tp.getFontMetrics().top);
                    }
                }
                // shift everything up, to compensate for the vertical movement caused by the font change below
                addSpanStyling(leftSeparatorSpan, new RelativeVerticalOffsetSpan(segmentedVerticalShiftRatio));
                addSpanStyling(likesSpan, new RelativeVerticalOffsetSpan(segmentedVerticalShiftRatio));
                addSpanStyling(middleSeparatorSpan, new RelativeVerticalOffsetSpan(segmentedVerticalShiftRatio));
                addSpanStyling(dislikeSpan, new RelativeVerticalOffsetSpan(segmentedVerticalShiftRatio));

                // important: must add size scaling after vertical offset (otherwise alignment gets off)
                addSpanStyling(leftSeparatorSpan, new RelativeSizeSpan(segmentedLeftSeparatorFontRatio));
                addSpanStyling(leftSeparatorSpan, new ScaleXSpan(segmentedLeftSeparatorHorizontalScaleRatio));
                // middle separator does not need resizing

                // put everything together
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(leftSeparatorSpan);
                builder.append(likesSpan);
                builder.append(middleSeparatorSpan);
                builder.append(dislikeSpan);
                replacementSpannable = new SpannableString(builder);
            }
        }

        textRef.set(replacementSpannable);
        return true;
    }

    private static boolean segmentedValuesSet = false;
    private static float segmentedVerticalShiftRatio;
    private static float segmentedLeftSeparatorFontRatio;
    private static float segmentedLeftSeparatorHorizontalScaleRatio;

    /**
     * Set the size/position adjustment values, based on the device.
     */
    private static void setSegmentedAdjustmentValues() {
        if (segmentedValuesSet) {
            return;
        }

        String deviceManufacturer = Build.MANUFACTURER;
        final int deviceSdkVersion = Build.VERSION.SDK_INT;
        DisplayMetrics metric = ReVancedUtils.getContext().getResources().getDisplayMetrics();
        final float deviceScaledDensity = metric.scaledDensity;
        final int deviceDensityDpi = metric.densityDpi;
        final int deviceHeight = metric.heightPixels;
        final int deviceWidth = metric.widthPixels;
        LogHelper.printDebug(() -> "Device manufacturer: '" + deviceManufacturer + "' SDK: " + deviceSdkVersion
                + " scaledDensity: " + deviceScaledDensity + " densityDpi:" + deviceDensityDpi
                + " width: " + deviceWidth + " height: " + deviceHeight);

        // device used to determine the configuration below
        final String configManufacturer; // very important
        final int configSdk; // important
        final float configScaledDensityDpi; // important only if using non default system font size
        final int configDensityDpi, configWidth, configHeight; // rarely important

        // If adding a configuration, then apply the new configuration to the
        // broadest number of devices while preserving existing settings.
        // ie: A new configuration for a Sony Android 13 phone should apply to all Sony phones.
        // ie: If later adding Sony Android 12, then that configuration applies only to Sony Android 12.
        // For logging/documentation variables, use the values in the log output above.
        //
        // IMPORTANT: configurations must be with the system font size default setting.
        // If you want to add font size specific configurations, then add multiple configurations based on scaledDensity.
        //
        // In generally, a single configuration will give perfect layout for all devices
        // with that manufacture and version of Android.
        // In general, fine tuning based on density and screen size is usually not needed.
        switch (deviceManufacturer) {
            default: // use Google layout by default
            case "Google":
                // logging and documentation
                configManufacturer = "Google";
                configSdk = 33;
                configScaledDensityDpi = 2.75f;
                configDensityDpi = 560;
                configWidth = 1080;
                configHeight = 2154;
                // actual adjustment values
                segmentedVerticalShiftRatio = -0.18f; // move separator and like/dislike up by 18%
                segmentedLeftSeparatorFontRatio = 1.8f;  // increase left separator size by 80%
                segmentedLeftSeparatorHorizontalScaleRatio = 0.75f; // horizontally compress left separator by 25%
                break;
            case "samsung":
                configManufacturer = "samsung";
                configSdk = 33;
                configScaledDensityDpi = 3.0f;
                configDensityDpi = 480;
                configWidth = 1080;
                configHeight = 2124;
                segmentedVerticalShiftRatio = -0.19f;
                segmentedLeftSeparatorFontRatio = 1.5f;
                segmentedLeftSeparatorHorizontalScaleRatio = 0.7f;
                break;
        }

        LogHelper.printDebug(() -> "Using layout adjustments based on manufacturer: '" + configManufacturer + "' SDK: " + configSdk
                + " scaledDensity: " + configScaledDensityDpi + " densityDpi: " + configDensityDpi + " width: " + configWidth + " height: " + configHeight);
        segmentedValuesSet = true;
    }

    /**
     * Correctly handles any unicode numbers (such as Arabic numbers)
     *
     * @return if the string contains at least 1 number
     */
    private static boolean stringContainsNumber(String text) {
        for (int index = 0, length = text.length(); index < length; index++) {
            if (Character.isDigit(text.codePointAt(index))) {
                return true;
            }
        }
        return false;
    }

    private static void addSpanStyling(Spannable destination, Object styling) {
        destination.setSpan(styling, 0, destination.length(), 0);
    }

    private static Spannable newSpannableWithDislikes(Spannable sourceStyling, RYDVoteData voteData) {
        return newSpanUsingStylingOfAnotherSpan(sourceStyling,
                SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getBoolean()
                        ? formatDislikePercentage(voteData.dislikePercentage)
                        : formatDislikeCount(voteData.dislikeCount));
    }

    private static Spannable newSpanUsingStylingOfAnotherSpan(Spannable sourceStyle, String newSpanText) {
        SpannableString destination = new SpannableString(newSpanText);
        Object[] spans = sourceStyle.getSpans(0, sourceStyle.length(), Object.class);
        for (Object span : spans) {
            destination.setSpan(span, 0, destination.length(), sourceStyle.getSpanFlags(span));
        }
        return destination;
    }

    private static String formatDislikeCount(long dislikeCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String formatted;
            synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
                if (dislikeCountFormatter == null) {
                    // Note: Java number formatters will use the locale specific number characters.
                    // such as Arabic which formats "1.2" into "١٫٢"
                    // But YouTube disregards locale specific number characters
                    // and instead shows english number characters everywhere.
                    Locale locale = ReVancedUtils.getContext().getResources().getConfiguration().locale;
                    LogHelper.printDebug(() -> "Locale: " + locale);
                    dislikeCountFormatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT);
                }
                formatted = dislikeCountFormatter.format(dislikeCount);
            }
            LogHelper.printDebug(() -> "Dislike count: " + dislikeCount + " formatted as: " + formatted);
            return formatted;
        }

        // never will be reached, as the oldest supported YouTube app requires Android N or greater
        return String.valueOf(dislikeCount);
    }

    private static String formatDislikePercentage(float dislikePercentage) {
        String formatted;
        synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
            if (dislikePercentageFormatter == null) {
                Locale locale = ReVancedUtils.getContext().getResources().getConfiguration().locale;
                LogHelper.printDebug(() -> "Locale: " + locale);
                dislikePercentageFormatter = NumberFormat.getPercentInstance(locale);
            }
            if (dislikePercentage >= 0.01) { // at least 1%
                dislikePercentageFormatter.setMaximumFractionDigits(0); // show only whole percentage points
            } else {
                dislikePercentageFormatter.setMaximumFractionDigits(1); // show up to 1 digit precision
            }
            formatted = dislikePercentageFormatter.format(dislikePercentage);
        }
        LogHelper.printDebug(() -> "Dislike percentage: " + dislikePercentage + " formatted as: " + formatted);
        return formatted;
    }


    /**
     * Number of times the UI was forced to wait on a network fetch to complete
     */
    private static volatile int numberOfTimesUIWaitedOnNetworkCalls;

    /**
     * Total time the UI waited, of all times it was forced to wait.
     */
    private static volatile long totalTimeUIWaitedOnNetworkCalls;

    private static void recordTimeUISpentWaitingForNetworkCall(long timeUIWaitStarted) {
        if (timeUIWaitStarted == 0 || !SettingsEnum.DEBUG.getBoolean()) {
            return;
        }
        final long timeUIWaitingTotal = System.currentTimeMillis() - timeUIWaitStarted;
        LogHelper.printDebug(() -> "UI thread waited for: " + timeUIWaitingTotal + "ms for vote fetch to complete");

        totalTimeUIWaitedOnNetworkCalls += timeUIWaitingTotal;
        numberOfTimesUIWaitedOnNetworkCalls++;
        final long averageTimeForcedToWait = totalTimeUIWaitedOnNetworkCalls / numberOfTimesUIWaitedOnNetworkCalls;
        LogHelper.printDebug(() -> "UI thread forced to wait: " + numberOfTimesUIWaitedOnNetworkCalls + " times, "
                + "total wait time: " + totalTimeUIWaitedOnNetworkCalls + "ms, "
                + "average wait time: " + averageTimeForcedToWait + "ms");
    }
}
