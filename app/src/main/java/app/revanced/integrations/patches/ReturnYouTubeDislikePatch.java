package app.revanced.integrations.patches;

import android.graphics.Rect;
import android.os.Build;
import android.text.*;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.patches.spoof.SpoofAppVersionPatch;
import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;

/**
 * Handles all interaction of UI patch components.
 *
 * Does not handle creating dislike spans or anything to do with {@link ReturnYouTubeDislikeApi}.
 */
public class ReturnYouTubeDislikePatch {

    @Nullable
    private static String currentVideoId;

    //
    // 17.x non litho player.
    //

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
            s.replace(0, s.length(), oldUIReplacementSpan); // Causes a recursive call back into this listener
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


    //
    // Litho player for both regular videos and Shorts.
    //

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
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return original;
            }

            String conversionContextString = conversionContext.toString();
            // Remove this log statement after the a/b new litho dislikes is fixed.
            LogHelper.printDebug(() -> "conversionContext: " + conversionContextString);

            final Spanned replacement;
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                // Regular video
                replacement = ReturnYouTubeDislike.getDislikesSpanForRegularVideo((Spannable) original, true);
                // When spoofing between 17.09.xx and 17.30.xx the UI is the old layout but uses litho
                // and the dislikes is "|dislike_button.eml|"
                // but spoofing to that range gives a broken UI layout so no point checking for this.
            } else if (SettingsEnum.RYD_SHORTS.getBoolean() && conversionContextString.contains("|shorts_dislike_button.eml|")) {
                // Litho shorts player
                replacement = ReturnYouTubeDislike.getDislikeSpanForShort((Spannable) original);
            } else {
                return original;
            }

            textRef.set(replacement);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "onLithoTextLoaded failure", ex);
        }
        return original;
    }


    //
    // Non litho shorts player.
    //

    /**
     * Replacement text to use for "Dislikes" while RYD is fetching.
     */
    private static final Spannable SHORTS_LOADING_SPAN = new SpannableString("-");

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
     * Injection point.  Called when a Shorts dislike is updated.
     * Handles update asynchronously, otherwise Shorts video will be frozen while the UI thread is blocked.
     *
     * @return if RYD is enabled and the TextView was updated
     */
    public static boolean setShortsDislikes(@NonNull View likeDislikeView) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return false;
            }
            if (!SettingsEnum.RYD_SHORTS.getBoolean()) {
                // Must clear the data here, in case a new video was loaded while PlayerType
                // suggested the video was not a short (can happen when spoofing to an old app version).
                ReturnYouTubeDislike.setCurrentVideoId(null);
                return false;
            }
            LogHelper.printDebug(() -> "setShortsDislikes");

            TextView textView = (TextView) likeDislikeView;
            textView.setText(SHORTS_LOADING_SPAN); // Change 'Dislike' text to the loading text
            shortsTextViewRefs.add(new WeakReference<>(textView));

            if (likeDislikeView.isSelected() && isShortTextViewOnScreen(textView)) {
                LogHelper.printDebug(() -> "Shorts dislike is already selected");
                ReturnYouTubeDislike.setUserVote(Vote.DISLIKE);
            }

            // For the first short played, the shorts dislike hook is called after the video id hook.
            // But for most other times this hook is called before the video id (which is not ideal).
            // Must update the TextViews here, and also after the videoId changes.
            updateOnScreenShortsTextViews(false);

            return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "setShortsDislikes failure", ex);
            return false;
        }
    }

    /**
     * @param forceUpdate if false, then only update the 'loading text views.
     *                    If true, update all on screen text views.
     */
    private static void updateOnScreenShortsTextViews(boolean forceUpdate) {
        try {
            clearRemovedShortsTextViews();
            if (shortsTextViewRefs.isEmpty()) {
                return;
            }

            LogHelper.printDebug(() -> "updateShortsTextViews");
            String videoId = VideoInformation.getVideoId();

            Runnable update = () -> {
                Spanned shortsDislikesSpan = ReturnYouTubeDislike.getDislikeSpanForShort(SHORTS_LOADING_SPAN);
                ReVancedUtils.runOnMainThreadNowOrLater(() -> {
                    if (!videoId.equals(VideoInformation.getVideoId())) {
                        // User swiped to new video before fetch completed
                        LogHelper.printDebug(() -> "Ignoring stale dislikes data for short: " + videoId);
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
                        if (isShortTextViewOnScreen(textView)
                                && (forceUpdate || textView.getText().toString().equals(SHORTS_LOADING_SPAN.toString()))) {
                            LogHelper.printDebug(() -> "Setting Shorts TextView to: " + shortsDislikesSpan);
                            textView.setText(shortsDislikesSpan);
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
            LogHelper.printException(() -> "updateOnScreenShortsTextViews failure", ex);
        }
    }

    /**
     * Check if a view is within the screen bounds.
     */
    private static boolean isShortTextViewOnScreen(@NonNull View view) {
        final int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[0] <= 0 && location[1] <= 0) { // Lower bound
            return false;
        }
        Rect windowRect = new Rect();
        view.getWindowVisibleDisplayFrame(windowRect); // Upper bound
        return location[0] < windowRect.width() && location[1] < windowRect.height();
    }


    //
    // Video Id and voting hooks (all players)
    //

    /**
     * Injection point.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        newVideoLoaded(videoId, true);
    }

    /**
     * @param isVideoInformationHook  If the video id is from {@link VideoInformation}.
     */
    public static void newVideoLoaded(@NonNull String videoId, boolean isVideoInformationHook) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) return;

            if (!videoId.equals(currentVideoId)) {
                final boolean isNoneOrHidden = PlayerType.getCurrent().isNoneOrHidden();
                if (isNoneOrHidden) {
                    if (!SettingsEnum.RYD_SHORTS.getBoolean()) {
                        // Clear the video id to prevent voting for the wrong video if spoofing to 16.x
                        currentVideoId = null;
                        ReturnYouTubeDislike.setCurrentVideoId(null);
                        return;
                    }
                    if (isVideoInformationHook && shortsTextViewRefs.isEmpty() && oldUITextViewRef.get() == null) {
                        // App is using the new litho shorts player, because no dislike text views were hooked.
                        // Do not set the video id here, and instead it's set from the litho filter.
                        currentVideoId = null;
                        LogHelper.printDebug(() -> "Ignoring VideoInformation hook as litho shorts player is in use for video: " + videoId);
                        return;
                    }
                }

                currentVideoId = videoId;
                ReturnYouTubeDislike.newVideoLoaded(videoId);

                if (isNoneOrHidden) {
                    // Shorts TextView hook can be called out of order with the video id hook.
                    // Must manually update again here.
                    updateOnScreenShortsTextViews(true);
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
            if (PlayerType.getCurrent().isNoneHiddenOrMinimized()) {
                if (!SettingsEnum.RYD_SHORTS.getBoolean()) return;
                // Because the video id is initially set from the Litho filter,
                // the last loaded short will be the next short (and not the current).
                // Fix this by setting the current short that is on screen now.
                ReturnYouTubeDislike.newVideoLoaded(VideoInformation.getVideoId());
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
