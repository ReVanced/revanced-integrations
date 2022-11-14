package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.utils.ResourceUtils.anim;
import static app.revanced.integrations.utils.ResourceUtils.findView;
import static app.revanced.integrations.utils.ResourceUtils.integer;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class VotingButton {
    @SuppressLint("StaticFieldLeak")
    static RelativeLayout _youtubeControlsLayout;
    static WeakReference<ImageView> _votingButton = new WeakReference<>(null);
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    static boolean isShowing;

    public static void initialize(Object viewStub) {
        try {
            _youtubeControlsLayout = (RelativeLayout) viewStub;
            ImageView view = findView(VotingButton.class, _youtubeControlsLayout, "voting_button");
            view.setOnClickListener(SponsorBlockUtils.voteButtonListener);
            _votingButton = new WeakReference<>(view);

            fadeDurationFast = integer("fade_duration_fast");
            fadeDurationScheduled = integer("fade_duration_scheduled");

            fadeIn = anim("fade_in");
            fadeIn.setDuration(fadeDurationFast);

            fadeOut = anim("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);

            isShowing = true;
            changeVisibilityImmediate(false);
        } catch (Exception ex) {
            LogHelper.printException(VotingButton.class, "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    public static void changeVisibilityNegatedImmediate(boolean visible) {
        changeVisibility(!visible, true);
    }

    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        if (isShowing == visible) return;
        isShowing = visible;

        ImageView view = _votingButton.get();
        if (_youtubeControlsLayout == null || view == null) return;

        if (visible && shouldBeShown()) {
            if (PlayerController.getLastKnownVideoTime() >= PlayerController.getCurrentVideoLength())
                return;
            view.setVisibility(View.VISIBLE);
            if (!immediate) view.startAnimation(fadeIn);
            return;
        }

        if (view.getVisibility() == View.VISIBLE) {
            if (!immediate) view.startAnimation(fadeOut);
            view.setVisibility(shouldBeShown() ? View.INVISIBLE : View.GONE);
        }
    }

    static boolean shouldBeShown() {
        return SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_VOTING_ENABLED.getBoolean();
    }
}
