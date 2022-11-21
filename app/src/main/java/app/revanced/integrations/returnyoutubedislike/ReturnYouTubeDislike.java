package app.revanced.integrations.returnyoutubedislike;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.text.SpannableString;

import androidx.annotation.GuardedBy;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislike {
    /** maximum amount of time to block the UI from updates, while waiting for dislike network call to complete */
    private static final long MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE = 5000;

    // different threads read and write these fields. access to them must be synchronized
    @GuardedBy("this") private static String currentVideoId;
    @GuardedBy("this") private static Integer dislikeCount;

    private static boolean isEnabled;
    private static boolean segmentedButton;

    public enum Vote {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0);

        public final int value;

        Vote(int value) {
            this.value = value;
        }
    }

    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static CompactDecimalFormat compactNumberFormatter;

    static {
        Context context = ReVancedUtils.getContext();
        Locale locale = context.getResources().getConfiguration().locale;
        LogHelper.debug(ReturnYouTubeDislike.class, "locale - " + locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            compactNumberFormatter = CompactDecimalFormat.getInstance(
                    locale,
                    CompactDecimalFormat.CompactStyle.SHORT
            );
        }
        onEnabledChange(SettingsEnum.RYD_ENABLED.getBoolean()); // call after all static fields are initialized
    }

    private void ReturnYouTubeDislike() { } // only static methods

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
        if (!enabled) {
            return;
        }
        if (registration == null) {
            registration = new Registration();
        }
        if (voting == null) {
            voting = new Voting(registration);
        }
    }

    public static synchronized String getCurrentVideoId() {
        return currentVideoId;
    }
    public static synchronized void setCurrentVideoId(String videoId) {
        Objects.requireNonNull(videoId);
        currentVideoId = videoId;
        dislikeCount = null;
    }

    /**
     * @return the dislikeCount for {@link #getCurrentVideoId()}.
     *         Returns NULL if the dislike is not yet loaded, or if the dislike network fetch failed.
     */
    public static synchronized Integer getDislikeCount() {
        return dislikeCount;
    }
    /**
     * @return true if the videoId parameter matches the current dislike request, and the set value was successful
     *         If videoID parameter does not match currentVideoId, then this call does nothing
     */
    public static synchronized boolean setCurrentDislikeCount(String videoId, Integer videoIdDislikeCount) {
        if (! videoId.equals(currentVideoId)) {
            return false;
        }
        dislikeCount = videoIdDislikeCount;
        return true;
    }

    private static void interruptDislikeFetchThreadIfRunning() {
        if (_dislikeFetchThread == null) return;
        try {
            Thread.State dislikeFetchThreadState = _dislikeFetchThread.getState();
            if (dislikeFetchThreadState != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislike.class, "Interrupting the fetch dislike thread of state: " + dislikeFetchThreadState);
                _dislikeFetchThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error in the fetch dislike thread", ex);
        }
    }
    private static void interruptVoteThreadIfRunning() {
        if (_votingThread == null) return;
        try {
            Thread.State voteThreadState = _votingThread.getState();
            if (voteThreadState != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislike.class, "Interrupting the voting thread of state: " + voteThreadState);
                _votingThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error in the voting thread", ex);
        }
    }

    public static void newVideoLoaded(String videoId) {
        if (!isEnabled) return;
        LogHelper.debug(ReturnYouTubeDislike.class, "newVideoLoaded - " + videoId);
        setCurrentVideoId(videoId);
        interruptDislikeFetchThreadIfRunning();

        // TODO use a private fixed size thread pool
        _dislikeFetchThread = new Thread(() -> ReturnYouTubeDislikeApi.fetchDislikes(videoId));
        _dislikeFetchThread.start();
    }

    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            var conversionContextString = conversionContext.toString();

            // Check for new component
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                segmentedButton = true;
            } else if (!conversionContextString.contains("|dislike_button.eml|")) {
                LogHelper.debug(ReturnYouTubeDislike.class, "could not find a dislike button in " + conversionContextString);
                return;
            }

            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            if (_dislikeFetchThread != null) {
                _dislikeFetchThread.join(MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE);
            }

            Integer fetchedDislikeCount = getDislikeCount();
            if (fetchedDislikeCount == null) {
                LogHelper.debug(ReturnYouTubeDislike.class, "timed out waiting for Dislike fetch thread to complete");
                // no point letting the request continue, as there is not another chance to use the result
                interruptDislikeFetchThreadIfRunning();
                return;
            }

            updateDislike(textRef, dislikeCount);
            LogHelper.debug(ReturnYouTubeDislike.class, "Updated text on component" + conversionContextString);
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error while trying to set dislikes text", ex);
        }
    }

    public static void sendVote(Vote vote) {
        if (!isEnabled) return;

        Context context = Objects.requireNonNull(ReVancedUtils.getContext());
        if (SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true))
            return;

        interruptVoteThreadIfRunning();
        String videoIdToVoteFor = getCurrentVideoId();
        LogHelper.debug(ReturnYouTubeDislike.class, "sending vote - " + vote + " for video " + videoIdToVoteFor);

        // TODO use a private fixed sized thread pool
        _votingThread = new Thread(() -> {
            try {
                boolean result = voting.sendVote(videoIdToVoteFor, vote);
                LogHelper.debug(ReturnYouTubeDislike.class, "sendVote status " + result);
            } catch (Exception ex) {
                LogHelper.printException(ReturnYouTubeDislike.class, "Failed to send vote", ex);
            }
        });
        _votingThread.start();
    }

    private static void updateDislike(AtomicReference<Object> textRef, Integer dislikeCount) {
        SpannableString oldSpannableString = (SpannableString) textRef.get();

        // parse the buttons string
        // if the button is segmented, only get the like count as a string
        var oldButtonString = oldSpannableString.toString();
        if (segmentedButton) oldButtonString = oldButtonString.split(" \\| ")[0];

        var dislikeString = formatDislikes(dislikeCount);
        SpannableString newString = new SpannableString(
                segmentedButton ? (oldButtonString + " | " + dislikeString) : dislikeString
        );

        // Copy style (foreground color, etc) to new string
        Object[] spans = oldSpannableString.getSpans(0, oldSpannableString.length(), Object.class);
        for (Object span : spans)
            newString.setSpan(span, 0, newString.length(), oldSpannableString.getSpanFlags(span));

        textRef.set(newString);
    }

    private static String formatDislikes(int dislikes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && compactNumberFormatter != null) {
            final String formatted = compactNumberFormatter.format(dislikes);
            LogHelper.debug(ReturnYouTubeDislike.class, "Formatting dislikes - " + dislikes + " - " + formatted);
            return formatted;
        }
        LogHelper.debug(ReturnYouTubeDislike.class, "Couldn't format dislikes, using the unformatted count - " + dislikes);
        return String.valueOf(dislikes);
    }
}
