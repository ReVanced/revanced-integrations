package app.revanced.integrations.patches;

import static app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import android.text.SpannableString;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

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
            SpannableString replacement = ReturnYouTubeDislike.getDislikeSpanForContext(conversionContext, original);
            if (replacement != null) {
                textRef.set(replacement);
                return replacement;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onLithoTextLoaded failure", ex);
        }
        return original;
    }


    /**
     * Resource identifier of old UI dislike button.
     */
    private static int OLD_UI_DISLIKE_BUTTON_RESOURCE_ID;

    /**
     * Injection point.
     *
     * Used when spoofing the older app versions of {@link SpoofAppVersionPatch}.
     */
    public static void setOldUILayoutDislikes(int resourceIdentifier, @NonNull TextView textView) {
        try {
            if (!SettingsEnum.RYD_ENABLED.getBoolean()) {
                return;
            }
            if (OLD_UI_DISLIKE_BUTTON_RESOURCE_ID == 0) {
                OLD_UI_DISLIKE_BUTTON_RESOURCE_ID = ReVancedUtils.getResourceIdentifier("dislike_button", "id");
                if (OLD_UI_DISLIKE_BUTTON_RESOURCE_ID == 0) {
                    LogHelper.printException(() -> "Could not find resource identifier");
                    return;
                }
            }
            if (resourceIdentifier != OLD_UI_DISLIKE_BUTTON_RESOURCE_ID) {
                return;
            }
            Spanned original = (Spanned) textView.getText();
            Runnable getDislikesAndSet = () -> {
                Spanned dislikes = ReturnYouTubeDislike.getDislikesSpanForRegularVideo(original, false);
                if (dislikes != null) {
                    ReVancedUtils.runOnMainThreadNowOrLater(() -> {
                        textView.setText(dislikes);
                    });
                }
            };
            if (ReturnYouTubeDislike.fetchDone()) {
                getDislikesAndSet.run(); // Run everything on main thread now, since no blocking will occur.
            } else {
                // Run on background thread and update whenever fetch completes.
                ReVancedUtils.runOnBackgroundThread(getDislikesAndSet);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "getOldLayoutDislikes failure", ex);
        }
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
            SpannableString replacement = ReturnYouTubeDislike.getDislikeSpanForShort(original);
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
