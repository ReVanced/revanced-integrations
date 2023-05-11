package app.revanced.integrations.patches;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;
import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.newSpanUsingStylingOfAnotherSpan;

import android.graphics.Rect;
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
            LogHelper.printDebug(() -> "setOldUILayoutDislikes");

            if (oldUIOriginalSpan == null) {
                // Use value of the first instance, as it appears TextViews can be recycled
                // and might contain dislikes previously added by the patch.
                oldUIOriginalSpan = (Spanned) textView.getText();
            }
            oldUITextViewRef = new WeakReference<>(textView);
            // No way to check if a listener is already attached, so remove and add again.
            textView.removeTextChangedListener(oldUiTextWatcher);
            textView.addTextChangedListener(oldUiTextWatcher);

            /**
             * If the patch is changed to include the dislikes button as a parameter to this method,
             * then if the button is already selected the dislikes could be adjusted using
             * {@link ReturnYouTubeDislike#setUserVote(Vote)}
             */

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
     * Replacement text to use for "Dislikes" while RYD is fetching.
     */
    private static final SpannableString SHORTS_LOADING_TEXT = new SpannableString("-");

    @Nullable
    private static boolean firstShortHasBeenPlayed;

    @Nullable
    private static Spanned currentShortsDislikesSpan;

    private static final TextWatcher shortsTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        public void afterTextChanged(Editable s) {
            if (currentShortsDislikesSpan == null || currentShortsDislikesSpan.toString().equals(s.toString())) {
                return;
            }
            s.replace(0, s.length(), currentShortsDislikesSpan);
        }
    };

    /**
     * Dislikes TextViews used by Shorts.
     *
     * Multiple TextViews are loaded at once (for the prior and next videos to swipe to).
     * Keep track of all of them, and later pick out the correct one based on their on screen position.
     */
    private static final List<WeakReference<TextView>> shortsTextViewRefs = new ArrayList<>();

    private static void clearRemovedShortsTextViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shortsTextViewRefs.removeIf(ref -> ref.get() == null);
            return;
        }
        throw new IllegalStateException(); // YouTube requires Android N or greater
    }

    /**
     * @return {@link #SHORTS_LOADING_TEXT} with the same styling as the TextView.
     */
    @NonNull
    private static SpannableString getShortsLoadingSpan(@NonNull TextView textView) {
        CharSequence text = textView.getText();
        Spanned textSpan = (text instanceof Spanned)
                ? (Spanned) text
                : new SpannableString(text);
        return newSpanUsingStylingOfAnotherSpan(textSpan, SHORTS_LOADING_TEXT);
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
            LogHelper.printDebug(() -> "updateShortsDislikes");

            TextView textView = (TextView) likeDislikeView;
            shortsTextViewRefs.add(new WeakReference<>(textView));
            // Change 'Dislike' text to the loading text
            textView.setText(getShortsLoadingSpan(textView));

            if (isShortTextViewOnScreen(textView) && likeDislikeView.isSelected()) {
                LogHelper.printDebug(() -> "Shorts dislike is already selected");
                ReturnYouTubeDislike.setUserVote(Vote.DISLIKE);
            }

            // For first short played, the shorts dislike hook is called after the video id hook
            // all other times this hook is called before the video id (which is not preferred).
            // Only update the shorts text views, if this is the first short played.
            // If the textviews are always updated here, then when changing shorts the next short
            // will momentarily show the prior shorts dislike count.
            if (!firstShortHasBeenPlayed) {
                firstShortHasBeenPlayed = true;
                updateOnScreenShortsTextView();
            }

            return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "updateShortsDislikes failure", ex);
            return false;
        }
    }

    private static void updateOnScreenShortsTextView() {
        try {
            clearRemovedShortsTextViews();
            if (shortsTextViewRefs.isEmpty()) {
                return;
            }

            LogHelper.printDebug(() -> "updateShortsTextViewsOnScreen");
            String videoId = VideoInformation.getVideoId();

            Runnable update = () -> {
                currentShortsDislikesSpan = ReturnYouTubeDislike.getDislikeSpanForShort(SHORTS_LOADING_TEXT);
                ReVancedUtils.runOnMainThreadNowOrLater(() -> {
                    if (!videoId.equals(VideoInformation.getVideoId())) {
                        // User swiped to new video before fetch completed
                        LogHelper.printDebug(() -> "Ignoring stale dislikes data for shorts: " + videoId);
                        return;
                    }

                    // Update text views that appear to be visible on screen.
                    // Only 1 will be the actual textview for the current Short,
                    // but discarded and not yet garbage collected views can remain.
                    // So must set the dislike span on all views that match.
                    for (WeakReference<TextView> textViewRef : shortsTextViewRefs) {
                        TextView textView = textViewRef.get();
                        if (textView == null) {
                            continue;
                        }
                        // Cannot check if a listener is already added, so remove and add again.
                        textView.removeTextChangedListener(shortsTextWatcher);
                        if (isShortTextViewOnScreen(textView)) {
                            textView.setText(currentShortsDislikesSpan);
                             // Prevent the text from being changed.
                            textView.addTextChangedListener(shortsTextWatcher);
                        }
                    }
                });
            };
            if (ReturnYouTubeDislike.fetchCompleted()) {
                update.run(); // Network call is completed, no need to wait on background thread.
            } else {
                ReVancedUtils.runOnBackgroundThread(update);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "updateShortsTextViewsOnScreen failure", ex);
        }
    }

    private static boolean isShortTextViewOnScreen(@NonNull View view) {
        // If a Shorts TextView is off screen, it appears to always have a window position of [0, 0]
        final int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[0] <= 0 && location[1] <= 0) {
            return false;
        }
        // Also check if it's within the window bounds, even though this check does not seem to be needed.
        Rect windowRect = new Rect();
        view.getWindowVisibleDisplayFrame(windowRect);
        return location[0] < windowRect.width() && location[1] < windowRect.height();
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
                    // When swiping to a new short, the short hook can be called before the video id hook.
                    // Must manually update the shorts dislike text views to fix these situations,
                    // otherwise the video will incorrectly show the dislikes of the prior short.
                    updateOnScreenShortsTextView();
                } else {
                    firstShortHasBeenPlayed = false; // Shorts player was closed
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
                    updateOnScreenShortsTextView();
                    return;
                }
            }
            LogHelper.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "sendVote failure", ex);
        }
    }
}
