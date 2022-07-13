package app.revanced.integrations.ryd;

import static app.revanced.integrations.sponsorblock.player.VideoInformation.currentVideoId;
import static app.revanced.integrations.sponsorblock.player.VideoInformation.dislikeCount;
import static app.revanced.integrations.utils.ReVancedUtils.getIdentifier;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Locale;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.ryd.requests.RYDRequester;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislikes {
    public static boolean isEnabled;
    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static CompactDecimalFormat compactNumberFormatter;

    static {
        Context context = ReVancedUtils.getContext();
        isEnabled = SettingsEnum.RYD_ENABLED_BOOLEAN.getBoolean();
        if (isEnabled) {
            registration = new Registration();
            voting = new Voting(registration);
        }

        Locale locale = context.getResources().getConfiguration().locale;
        LogHelper.debug(ReturnYouTubeDislikes.class, "locale - " + locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            compactNumberFormatter = CompactDecimalFormat.getInstance(
                    locale,
                    CompactDecimalFormat.CompactStyle.SHORT
            );
        }
    }

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
        if (registration == null) {
            registration = new Registration();
        }
        if (voting == null) {
            voting = new Voting(registration);
        }
    }

    // Call hook in the YT code when the video changes
    public static void newVideoLoaded(String videoId) {
        LogHelper.debug(ReturnYouTubeDislikes.class, "newVideoLoaded - " + videoId);

        dislikeCount = null;
        if (!isEnabled) return;

        try {
            if (_dislikeFetchThread != null && _dislikeFetchThread.getState() != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislikes.class, "Interrupting the thread. Current state " + _dislikeFetchThread.getState());
                _dislikeFetchThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikes.class, "Error in the dislike fetch thread", ex);
        }

        _dislikeFetchThread = new Thread(() -> RYDRequester.fetchDislikes(videoId));
        _dislikeFetchThread.start();
    }

    // Call hook in the YT code when a litho component gets created
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            // Contains a pathBuilder string, used to distinguish from other litho components
            if (!conversionContext.toString().contains("dislike_button")) return;

            LogHelper.debug(ReturnYouTubeDislikes.class, "dislike button was created");

            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            if (_dislikeFetchThread != null) _dislikeFetchThread.join();

            if (dislikeCount != null) {
                updateDislikeText(textRef, formatDislikes(dislikeCount));
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while trying to set dislikes text", ex);
        }
    }

    // Call hook in the YT code when the like/dislike button gets clicked
    // @param vote -1 (dislike), 0 (none) or 1 (like)
    public static void sendVote(int vote) {
        if (!isEnabled) return;

        Context context = ReVancedUtils.getContext();
        if (SharedPrefHelper.getBoolean(Objects.requireNonNull(context), SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true))
            return;

        LogHelper.debug(ReturnYouTubeDislikes.class, "sending vote - " + vote + " for video " + currentVideoId);
        try {
            if (_votingThread != null && _votingThread.getState() != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislikes.class, "Interrupting the thread. Current state " + _votingThread.getState());
                _votingThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikes.class, "Error in the voting thread", ex);
        }

        _votingThread = new Thread(() -> {
            try {
                boolean result = voting.sendVote(currentVideoId, vote);
                LogHelper.debug(ReturnYouTubeDislikes.class, "sendVote status " + result);
            } catch (Exception ex) {
                LogHelper.printException(ReturnYouTubeDislikes.class, "Failed to send vote", ex);
            }
        });
        _votingThread.start();
    }

    private static void updateDislikeText(AtomicReference<Object> textRef, String text) {
        SpannableString oldString = (SpannableString) textRef.get();
        SpannableString newString = new SpannableString(text);

        // Copy style (foreground color, etc) to new string
        Object[] spans = oldString.getSpans(0, oldString.length(), Object.class);
        for (Object span : spans) {
            int flags = oldString.getSpanFlags(span);
            newString.setSpan(span, 0, newString.length(), flags);
        }

        textRef.set(newString);
    }

    private static String formatDislikes(int dislikes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && compactNumberFormatter != null) {
            final String formatted = compactNumberFormatter.format(dislikes);
            LogHelper.debug(ReturnYouTubeDislikes.class, "Formatting dislikes - " + dislikes + " - " + formatted);
            return formatted;
        }
        LogHelper.debug(ReturnYouTubeDislikes.class, "Couldn't format dislikes, using the unformatted count - " + dislikes);
        return String.valueOf(dislikes);
    }
}
