package app.revanced.integrations.patches;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;

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
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReturnYouTubeDislikePatch {

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
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) return;
            ReturnYouTubeDislike.newVideoLoaded(videoId);
        } catch (Exception ex) {
            LogHelper.printException(() -> "newVideoLoaded failure", ex);
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
     * Last Shorts dislikes span. Used to temporarily set the shorts span while waiting on fetch call.
     */
    @Nullable
    private static Spanned lastShortsSpan;

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

            TextView textView = (TextView)likeDislikeView;
            CharSequence text = textView.getText();
            Spanned textSpan = (text instanceof Spanned)
                    ? (Spanned) text
                    : new SpannableString(text);

            String videoId = VideoInformation.getVideoId();
            Runnable update = () -> {
                Spanned dislikesSpan = ReturnYouTubeDislike.getDislikeSpanForShort(textSpan);
                ReVancedUtils.runOnMainThreadNowOrLater(() -> {
                    if (!videoId.equals(VideoInformation.getVideoId())) {
                        // User swiped to new video before fetch completed.
                        LogHelper.printDebug(() -> "Ignoring stale dislikes data for shorts: " + videoId);
                        return;
                    }
                    lastShortsSpan = dislikesSpan;
                    textView.setText(dislikesSpan); // if RYD fails, this sets the text back to "Dislike"
                });
            };

            if (ReturnYouTubeDislike.fetchCompleted()) {
                update.run(); // Network call is completed, no need to wait on background thread.
            } else {
                // When swiping thru shorts, the dislikes text view will initially show the prior values of the video,
                // and just before this hook is called the text to 'Dislike'.
                // If the 'dislikes' is kept while the loading happens, then the text will flicker or blink
                // because most RYD api calls finish a few milliseconds after the text is reset to 'dislike'.
                // To keep the UI from flickering, continue to keep the prior dislikes text visible until the api call completes.
                // Most RYD calls take around 250ms, and after no more than 4000ms the dislikes will either be the correct value
                // or reset back to the "Dislike" text
                if (lastShortsSpan != null) {
                    textView.setText(lastShortsSpan);
                }
                ReVancedUtils.runOnBackgroundThread(update);
            }

            return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "onShortsComponentCreated failure", ex);
            return false;
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
                    return;
                }
            }
            LogHelper.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "sendVote failure", ex);
        }
    }
}
