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
    private static View _dislikeView = null;
    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static boolean likeActive;
    private static boolean dislikeActive;
    private static int votingValue = 0; // 1 = like, -1 = dislike, 0 = no vote
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

    //Was called in SB->player->VideoInformation->setCurrentVideoId(final String videoId) before, has to be called on its own at the same place now.
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

    // Call to this needs to be injected in YT code
//    public static void setLikeTag(View view) {
//        if (!isEnabled) return;
//
//        setTag(view, "like");
//    }

//    public static void setLikeTag(View view, boolean active) {
//        if (!isEnabled) return;
//
//        likeActive = active;
//        if (likeActive) {
//            votingValue = 1;
//        }
//
//        LogHelper.debug(ReturnYouTubeDislikes.class, "Like tag active " + likeActive);
//        setTag(view, "like");
//    }

    // Call to this needs to be injected in YT code
//    public static void setDislikeTag(View view) {
//        if (!isEnabled) return;
//
//        _dislikeView = view;
//        setTag(view, "dislike");
//    }

//    public static void setDislikeTag(View view, boolean active) {
//        if (!isEnabled) return;
//
//        dislikeActive = active;
//        if (dislikeActive) {
//            votingValue = -1;
//        }
//        _dislikeView = view;
//        LogHelper.debug(ReturnYouTubeDislikes.class, "Dislike tag active " + dislikeActive);
//        setTag(view, "dislike");
//    }

    // Call to this needs to be injected in YT code
    public static void onComponentCreated(Object conversionContext, AtomicReference textRef) {
        if (!isEnabled) return;

        try {
            // Contains a pathBuilder string, used to distinguish from other litho components
            if (!conversionContext.toString().contains("dislike_button")) return;

            // Have to block the current thread until fetching is done
            // There's no known way yet to edit the text asynchronously
            if (_dislikeFetchThread != null) _dislikeFetchThread.join();

            if (dislikeCount != null) {
                updateDislikeText(textRef);
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while trying to set dislikes text", ex);
        }
    }

    private static void updateDislikeText(AtomicReference<SpannableString> textRef) {
        SpannableString oldString = textRef.get();
        SpannableString newString = new SpannableString(formatDislikes(dislikeCount));

        // Copy style (foreground color, etc) to new string
        Object[] spans = oldString.getSpans(0, oldString.length(), Object.class);
        for (Object span : spans) {
            int flags = oldString.getSpanFlags(span);
            newString.setSpan(span, 0, newString.length(), flags);
        }
        textRef.set(newString);
    }

    // Call to this needs to be injected in YT code
    public static void onClick(View view, boolean inactive) {
        if (!isEnabled) return;

        handleOnClick(view, inactive);
    }

//    private static CharSequence handleOnSetText(View view, CharSequence originalText) {
//        if (!isEnabled) return originalText;
//
//        try {
//            CharSequence tag = (CharSequence) view.getTag();
//            LogHelper.debug(ReturnYouTubeDislikes.class, "handleOnSetText - " + tag + " - original text - " + originalText);
//            if (tag == null) return originalText;
//
//            if (tag == "like") {
//                return originalText;
//            } else if (tag == "dislike") {
//                return dislikeCount != null ? formatDislikes(dislikeCount) : originalText;
//            }
//        } catch (Exception ex) {
//            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while handling the setText", ex);
//        }
//
//        return originalText;
//    }

//    public static void trySetDislikes(String dislikeCount) {
//        if (!isEnabled) return;
//
//        try {
//            // Try to set normal video dislike count
//            if (_dislikeView == null) {
//                LogHelper.debug(ReturnYouTubeDislikes.class, "_dislikeView was null");
//                return;
//            }
//
//            View buttonView = _dislikeView.findViewById(getIdentifier("button_text", "id"));
//            if (buttonView == null) {
//                LogHelper.debug(ReturnYouTubeDislikes.class, "buttonView was null");
//                return;
//            }
//            TextView button = (TextView) buttonView;
//            button.setText(dislikeCount);
//            LogHelper.debug(ReturnYouTubeDislikes.class, "trySetDislikes - " + dislikeCount);
//        } catch (Exception ex) {
//            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while trying to set dislikes text", ex);
//        }
//    }

    private static void handleOnClick(View view, boolean previousState) {
        Context context = ReVancedUtils.getContext();
        if (!isEnabled || SharedPrefHelper.getBoolean(Objects.requireNonNull(context), SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true))
            return;

        try {
            String tag = (String) view.getTag();
            LogHelper.debug(ReturnYouTubeDislikes.class, "handleOnClick - " + tag + " - previousState - " + previousState);
            if (tag == null) return;

            // If active status was removed, vote should be none
            if (previousState) {
                votingValue = 0;
            }
            if (tag.equals("like")) {

                // Like was activated
                if (!previousState) {
                    votingValue = 1;
                    likeActive = true;
                } else {
                    likeActive = false;
                }

                // Like was activated and dislike was previously activated
                if (!previousState && dislikeActive) {
                    dislikeCount--;
//                    trySetDislikes(formatDislikes(dislikeCount));
                }
                dislikeActive = false;
            } else if (tag.equals("dislike")) {
                likeActive = false;

                // Dislike was activated
                if (!previousState) {
                    votingValue = -1;
                    dislikeActive = true;
                    dislikeCount++;
                }
                // Dislike was removed
                else {
                    dislikeActive = false;
                    dislikeCount--;
                }
//                trySetDislikes(formatDislikes(dislikeCount));
            } else {
                // Unknown tag
                return;
            }

            LogHelper.debug(ReturnYouTubeDislikes.class, "New vote status - " + votingValue);
            LogHelper.debug(ReturnYouTubeDislikes.class, "Like button " + likeActive + " | Dislike button " + dislikeActive);
            sendVote(votingValue);
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while handling the onClick", ex);
        }
    }

    private static void sendVote(int vote) {
        if (!isEnabled) return;

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

//    private static void setTag(View view, String tag) {
//        if (!isEnabled) return;
//
//        try {
//            if (view == null) {
//                LogHelper.debug(ReturnYouTubeDislikes.class, "View was empty");
//                return;
//            }
//            LogHelper.debug(ReturnYouTubeDislikes.class, "setTag - " + tag);
//            view.setTag(tag);
//        } catch (Exception ex) {
//            LogHelper.printException(ReturnYouTubeDislikes.class, "Error while trying to set tag to view", ex);
//        }
//    }

    public static String formatDislikes(int dislikes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && compactNumberFormatter != null) {
            final String formatted = compactNumberFormatter.format(dislikes);
            LogHelper.debug(ReturnYouTubeDislikes.class, "Formatting dislikes - " + dislikes + " - " + formatted);
            return formatted;
        }
        LogHelper.debug(ReturnYouTubeDislikes.class, "Couldn't format dislikes, using the unformatted count - " + dislikes);
        return String.valueOf(dislikes);
    }
}
