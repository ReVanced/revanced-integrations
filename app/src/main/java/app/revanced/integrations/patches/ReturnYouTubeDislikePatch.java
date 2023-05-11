package app.revanced.integrations.patches;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;
import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.newSpanUsingStylingOfAnotherSpan;

import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Handles all interaction of UI patch components.
 *
 * Does not handle creating dislike spans or anything to do with {@link ReturnYouTubeDislikeApi}.
 */
public class ReturnYouTubeDislikePatch {

    @Nullable
    private static String currentVideoId;

    /**
     * Resource identifier of old UI dislike button.
     */
    private static final int OLD_UI_DISLIKE_BUTTON_RESOURCE_ID
            = ReVancedUtils.getResourceIdentifier("dislike_button", "id");

    /**
     * Dislikes text label used by old UI.
     */
    @NonNull
    private static WeakReference<TextView> oldUITextViewRef = new WeakReference<>(null);

    /**
     * Original old UI 'Dislikes' text before patch modifications.
     * Required to reset the dislikes when changing videos and RYD is not available.
     * Set only once during the first load.
     */
    private static Spanned oldUIOriginalSpan;

    /**
     * Replacement span that contains dislike value. Used by {@link #oldUiTextWatcher}.
     */
    @Nullable
    private static Spanned oldUIReplacementSpan;

    /**
     * Old UI dislikes can be set multiple times by YouTube.
     * To prevent it from reverting changes made here, this listener overrides any future changes YouTube makes.
     */
    private static final TextWatcher oldUiTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        public void afterTextChanged(Editable s) {
            if (oldUIReplacementSpan == null || oldUIReplacementSpan.toString().equals(s.toString())) {
                return;
            }
            s.replace(0, s.length(), oldUIReplacementSpan);
        }
    };

    private static void updateOldUIDislikesTextView() {
        TextView oldUITextView = oldUITextViewRef.get();
        if (oldUITextView == null) {
            return;
        }
        oldUIReplacementSpan = ReturnYouTubeDislike.getDislikesSpanForRegularVideo(oldUIOriginalSpan, false);
        if (!oldUIReplacementSpan.equals(oldUITextView.getText())) {
            oldUITextView.setText(oldUIReplacementSpan);
        }
    }

    /**
     * Injection point.  Called on main thread.
     *
     * Used when spoofing the older app versions of {@link SpoofAppVersionPatch}.
     */
    public static void setOldUILayoutDislikes(int buttonViewResourceId, @Nullable TextView textView) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()
                    || buttonViewResourceId != OLD_UI_DISLIKE_BUTTON_RESOURCE_ID
                    || textView == null) {
                return;
            }
            if (oldUIOriginalSpan == null) {
                // Use value of the first instance, as it appears TextViews can be recycled
                // and might contain dislikes previously added by the patch.
                oldUIOriginalSpan = (Spanned) textView.getText();
            }
            oldUITextViewRef = new WeakReference<>(textView);
            // No way to check if a listener is already attached, so remove and add again.
            textView.removeTextChangedListener(oldUiTextWatcher);
            textView.addTextChangedListener(oldUiTextWatcher);

            updateOldUIDislikesTextView();
        } catch (Exception ex) {
            LogHelper.printException(() -> "setOldUILayoutDislikes failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * Called when a litho text component is initially created,
     * and also when a Span is later reused again (such as scrolling off/on screen).
     *
     * This method is sometimes called on the main thread, but it usually is called _off_ the main thread.
     * This method can be called multiple times for the same UI element (including after dislikes was added).
     *
     * @param textRef Cache reference to the like/dislike char sequence,
     *                which may or may not be the same as the original span parameter.
     *                If dislikes are added, the atomic reference must be set to the replacement span.
     * @param original Original span that was created or reused by Litho.
     * @return The original span (if nothing should change), or a replacement span that contains dislikes.
     */
    @NonNull
    public static CharSequence onLithoTextLoaded(@NonNull Object conversionContext,
                                                 @NonNull AtomicReference<CharSequence> textRef,
                                                 @NonNull CharSequence original) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean() || PlayerType.getCurrent().isNoneOrHidden()) {
                return original;
            }

            String conversionContextString = conversionContext.toString();
            final boolean isSegmentedButton;
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                isSegmentedButton = true;
            } else if (conversionContextString.contains("|dislike_button.eml|")) {
                isSegmentedButton = false;
            } else {
                return original;
            }

            Spanned replacement = ReturnYouTubeDislike.getDislikesSpanForRegularVideo((Spannable) original, isSegmentedButton);
            textRef.set(replacement);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "onLithoTextLoaded failure", ex);
        }
        return original;
    }

    /**
     * Replacement text to use for "Dislikes" while RYD fetch is completing.
     */
    private static final String SHORTS_LOADING_TEXT = "-";

    /**
     * Dislikes TextViews used by Shorts.
     *
     * Because multiple TextViews are loaded at once (for the prior and next videos to swipe to),
     * and there's no way to distinguish which button goes to which video (and they can be loaded out of order),
     * and the Shorts text hook and the video id hooks can be called out of order,
     * The only simple solution is to apply the current video dislike to all of these text views.
     *
     * This means that when swiping to a new video, the dislikes of the prior video will show up for a moment.
     */
    @Nullable
    private static final List<WeakReference<TextView>> shortsTextViewRefs = new ArrayList<>();

    private static void removeDisposedShortsTextViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // YouTube requires Android N or greater
            shortsTextViewRefs.removeIf(ref -> ref.get() == null );
            return;
        }
        throw new IllegalStateException();
    }

    private static void setShortsDislikeText(CharSequence span) {
        for (WeakReference<TextView> textViewRef : shortsTextViewRefs) {
            TextView textView = textViewRef.get();
            if (textView == null) {
                continue;
            }
            textView.setText(span);
        }
    }

    private static Spanned getShortsDislikeLoadingSpan() {
        for (WeakReference<TextView> textViewRef : shortsTextViewRefs) {
            TextView textView = textViewRef.get();
            if (textView == null) {
                continue;
            }
            CharSequence text = textView.getText();
            Spanned textSpan = (text instanceof Spanned)
                    ? (Spanned) text
                    : new SpannableString(text);
            return newSpanUsingStylingOfAnotherSpan(textSpan, SHORTS_LOADING_TEXT);
        }
        // lacks styling of the TextView, but this should never be reached.
        return new SpannableString(SHORTS_LOADING_TEXT);
    }

    /**
     * Injection point.  Called when a Shorts dislike is updated.
     * Handles update asynchronously, otherwise Shorts video will be frozen while the UI thread is blocked.
     *
     * @return if RYD is enabled and the TextView was updated
     */
    public static boolean updateShortsDislikes(@NonNull View likeDislikeView) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return false;
            }

            removeDisposedShortsTextViews();
            shortsTextViewRefs.add(new WeakReference<>((TextView) likeDislikeView));
            updateAllShortsTextViews();

            return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "updateShortsDislikes failure", ex);
            return false;
        }
    }

    private static void updateAllShortsTextViews() {
        try {
            removeDisposedShortsTextViews();
            if (shortsTextViewRefs.isEmpty()) {
                return;
            }

            LogHelper.printDebug(() -> "updateAllShortsTextViews");
            String videoId = VideoInformation.getVideoId();
            Spanned loadingSpan = getShortsDislikeLoadingSpan();

            Runnable update = () -> {
                Spanned dislikesSpan = ReturnYouTubeDislike.getDislikeSpanForShort(loadingSpan);
                ReVancedUtils.runOnMainThreadNowOrLater(() -> {
                    if (!videoId.equals(VideoInformation.getVideoId())) {
                        // User swiped to new video before fetch completed.
                        // Or the Shorts hook was called before the video id hook (very common when swiping thru shorts).
                        // If the shorts hook was called early, then this code will run again after the dislikes are updated.
                        LogHelper.printDebug(() -> "Ignoring stale dislikes data for shorts: " + videoId);
                        return;
                    }
                    setShortsDislikeText(dislikesSpan);
                });
            };
            if (ReturnYouTubeDislike.fetchCompleted()) {
                update.run(); // Network call is completed, no need to wait on background thread.
            } else {
                setShortsDislikeText(loadingSpan);
                ReVancedUtils.runOnBackgroundThread(update);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "updateAllShortsTextViews failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) return;
            ReturnYouTubeDislike.newVideoLoaded(videoId);

            if (!videoId.equals(currentVideoId)) {
                currentVideoId = videoId;
                if (PlayerType.getCurrent().isNoneOrHidden()) {
                    // When swiping to a new short, the short hook is called before the video id hook.
                    // Must manually update the shorts dislike text views.
                    updateAllShortsTextViews();
                } else if (!shortsTextViewRefs.isEmpty()) {
                    LogHelper.printDebug(() -> "Clearing Shorts TextView");
                    shortsTextViewRefs.clear(); // Shorts player is no longer opened.
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "newVideoLoaded failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * Called when the user likes or dislikes.
     *
     * @param vote int that matches {@link ReturnYouTubeDislike.Vote#value}
     */
    public static void sendVote(int vote) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return;
            }

            for (Vote v : Vote.values()) {
                if (v.value == vote) {
                    ReturnYouTubeDislike.sendVote(v);

                    updateOldUIDislikesTextView();
                    updateAllShortsTextViews();
                    return;
                }
            }
            LogHelper.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "sendVote failure", ex);
        }
    }
}
