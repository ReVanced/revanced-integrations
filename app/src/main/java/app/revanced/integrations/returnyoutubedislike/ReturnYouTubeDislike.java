package app.revanced.integrations.returnyoutubedislike;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.text.SpannableString;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislike {
    /**
     * Maximum amount of time to block the UI from updates, while waiting for dislike network call to complete
     */
    private static final long MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE = 5000;

    /**
     * Used to send votes, one by one, in the same order the user created them
     */
    private static final ExecutorService voteSerialExecutor = Executors.newSingleThreadExecutor();

    // Must be volatile, since non-main threads read this field.
    private static volatile boolean isEnabled = SettingsEnum.RYD_ENABLED.getBoolean();

    /**
     * Used to guard {@link #currentVideoId} and {@link #dislikeFetchFuture},
     * as multiple threads access this class.
     */
    private static final Object videoIdLockObject = new Object();

    @GuardedBy("videoIdLockObject")
    private static String currentVideoId;

    /**
     * Stores the results of the dislike fetch, and used as a barrier to wait until fetch completes
     */
    @GuardedBy("videoIdLockObject")
    private static Future<Integer> dislikeFetchFuture;

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
    @GuardedBy("ReturnYouTubeDislike.class") // number formatter is not thread safe
    private static CompactDecimalFormat compactNumberFormatter;

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
    }

    private static String getCurrentVideoId() {
        synchronized (videoIdLockObject) {
            return currentVideoId;
        }
    }

    private static Future<Integer> getDislikeFetchFuture() {
        synchronized (videoIdLockObject) {
            return dislikeFetchFuture;
        }
    }

    // It is unclear if this method is always called on the main thread (since the YouTube app is the one making the call)
    // treat this as if any thread could call this method
    public static void newVideoLoaded(String videoId) {
        if (!isEnabled) return;
        try {
            Objects.requireNonNull(videoId);
            LogHelper.debug(ReturnYouTubeDislike.class, "New video loaded: " + videoId);

            synchronized (videoIdLockObject) {
                currentVideoId = videoId;
                // no need to wrap the fetchDislike call in a try/catch,
                // as any exceptions are propagated out in the later Future#Get call
                dislikeFetchFuture = ReVancedUtils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchDislikes(videoId));
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Failed to process new video id: " + videoId, ex);
        }
    }

    // BEWARE! This method is sometimes called on the main thread, but it usually is called _off_ the main thread!
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            var conversionContextString = conversionContext.toString();

            boolean isSegmentedButton = false;
            // Check for new component
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                isSegmentedButton = true;
            } else if (!conversionContextString.contains("|dislike_button.eml|")) {
                LogHelper.debug(ReturnYouTubeDislike.class, "ignoring UI component: " + conversionContextString);
                return;
            }

            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            Integer dislikeCount;
            try {
                dislikeCount = getDislikeFetchFuture().get(MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LogHelper.debug(ReturnYouTubeDislike.class, "UI timed out waiting for dislike fetch to complete");
                return;
            }
            if (dislikeCount == null) {
                LogHelper.debug(ReturnYouTubeDislike.class, "Cannot add dislike count to UI (dislike count not available)");
                return;
            }

            updateDislike(textRef, isSegmentedButton, dislikeCount);
            LogHelper.debug(ReturnYouTubeDislike.class, "Updated text on component" + conversionContextString);
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error while trying to set dislikes text", ex);
        }
    }

    public static void sendVote(Vote vote) {
        if (!isEnabled) return;
        try {
            Context context = Objects.requireNonNull(ReVancedUtils.getContext());
            if (SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true))
                return;

            // Must make a local copy of videoId, since it may change between now and when the vote thread runs
            String videoIdToVoteFor = getCurrentVideoId();

            voteSerialExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // must wrap in try/catch to properly log exceptions
                    try {
                        ReturnYouTubeDislikeApi.sendVote(videoIdToVoteFor, getUserId(), vote);
                    } catch (Exception ex) {
                        LogHelper.printException(ReturnYouTubeDislike.class, "Failed to send vote", ex);
                    }
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error while trying to send vote", ex);
        }
    }

    /**
     * Must call off main thread, as this will make a network call if user has not yet been registered yet
     *
     * @return ReturnYouTubeDislike user ID. If user registration has never happened
     * and the network call fails, this will return NULL
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

    private static void updateDislike(AtomicReference<Object> textRef, boolean isSegmentedButton, Integer dislikeCount) {
        SpannableString oldSpannableString = (SpannableString) textRef.get();

        // Parse the buttons string.
        // If the button is segmented, only get the like count as a string
        var oldButtonString = oldSpannableString.toString();
        if (isSegmentedButton) oldButtonString = oldButtonString.split(" \\| ")[0];

        var dislikeString = formatDislikeCount(dislikeCount);
        SpannableString newString = new SpannableString(
                isSegmentedButton ? (oldButtonString + " | " + dislikeString) : dislikeString
        );

        // Copy style (foreground color, etc) to new string
        Object[] spans = oldSpannableString.getSpans(0, oldSpannableString.length(), Object.class);
        for (Object span : spans)
            newString.setSpan(span, 0, newString.length(), oldSpannableString.getSpanFlags(span));

        textRef.set(newString);
    }

    private static String formatDislikeCount(int dislikeCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String formatted;
            synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
                if (compactNumberFormatter == null) {
                    Context context = ReVancedUtils.getContext();
                    Locale locale = context.getResources().getConfiguration().locale;
                    LogHelper.debug(ReturnYouTubeDislike.class, "Locale: " + locale);
                    compactNumberFormatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT);
                }
                formatted = compactNumberFormatter.format(dislikeCount);
            }
            LogHelper.debug(ReturnYouTubeDislike.class, "Formatting dislikes - " + dislikeCount + " - " + formatted);
            return formatted;
        }
        LogHelper.debug(ReturnYouTubeDislike.class, "Couldn't format dislikes, using the unformatted count - " + dislikeCount);
        return String.valueOf(dislikeCount);
    }
}
