package app.revanced.integrations.patches;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import android.text.SpannableString;
import android.text.Spanned;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class ReturnYouTubeDislikePatch {

    /**
     * Injection point.
     */
    public static void newVideoLoaded(String videoId) {
        ReturnYouTubeDislike.newVideoLoaded(videoId);
    }

    /**
     * Injection point.
     *
     * Called when a litho text component is initially created or if a video is liked/disliked.
     *
     * This method is sometimes called on the main thread, but it usually is called _off_ the main thread.
     * This method can be called multiple times for the same UI element (including after dislikes was added).
     *
     * @param textRef atomic reference should always be non null, but the object reference inside can be null.
     */
    public static void onComponentCreated(@NonNull Object conversionContext, @NonNull AtomicReference<CharSequence> textRef) {
        try {
            CharSequence original = textRef.get();
            if (original instanceof Spanned) {
                SpannableString replacement = ReturnYouTubeDislike.getDislikeSpanForContext(conversionContext, (Spanned) original);
                if (replacement != null) {
                    textRef.set(replacement);
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onComponentCreated AtomicReference failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * Identical to {@link #onComponentCreated(Object, AtomicReference)},
     * Except this is called when a Span reappears on screen after scrolling.
     * This is not called when a video is liked or disliked.
     */
    public static CharSequence onComponentCreated(@NonNull Object conversionContext, @NonNull CharSequence original) {
        try {
            if (original instanceof Spanned) {
                SpannableString dislikes = ReturnYouTubeDislike.getDislikeSpanForContext(conversionContext, (Spanned) original);
                if (dislikes != null) {
                    return dislikes;
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onComponentCreated CharSequence failure", ex);
        }
        return original;
    }

    /**
     * Injection point.
     *
     * Called when a Shorts dislike Spanned is created.
     */
    public static Spanned onShortsComponentCreated(Spanned dislike) {
        return ReturnYouTubeDislike.onShortsComponentCreated(dislike);
    }

    /**
     * Injection point.
     *
     * Called when the like/dislike button is clicked.
     *
     * @param vote int that matches {@link ReturnYouTubeDislike.Vote#value}
     */
    public static void sendVote(int vote) {
        if (!SettingsEnum.RYD_ENABLED.getBoolean()) return;

        try {
            for (Vote v : Vote.values()) {
                if (v.value == vote) {
                    ReturnYouTubeDislike.sendVote(v);
                    return;
                }
            }
            LogHelper.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "sendVote failure", ex);
        }
    }
}
