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
     * Required to update the UI after users dislikes.
     *
     * @param textRef Reference to the dislike char sequence.
     */
    public static void onComponentCreated(@NonNull Object conversionContext, @NonNull AtomicReference<CharSequence> textRef) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return;
            }
            CharSequence original = textRef.get(); // CharSequence can be null
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
     * Called when a litho text component is initially created and when reappearing on screen after scrolling.
     *
     * This method is sometimes called on the main thread, but it usually is called _off_ the main thread.
     * This method can be called multiple times for the same UI element (including after dislikes was added).
     */
    public static CharSequence onComponentCreated(@NonNull Object conversionContext, @NonNull CharSequence original) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return original;
            }
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
    public static Spanned onShortsComponentCreated(Spanned original) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return original;
            }
            Spanned replacement = ReturnYouTubeDislike.onShortsComponentCreated(original);
            if (replacement != null) {
                return replacement;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onShortsComponentCreated failure", ex);
        }
        return original;
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
                    return;
                }
            }
            LogHelper.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "sendVote failure", ex);
        }
    }
}
