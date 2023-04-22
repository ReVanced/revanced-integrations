package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdentifier;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SegmentPlaybackController;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class VotingButtonController {
    private static WeakReference<ImageView> buttonReference = new WeakReference<>(null);
    private static Animation fadeIn;
    private static Animation fadeOut;
    private static boolean isShowing;

    /**
     * injection point
     */
    public static void initialize(Object viewStub) {
        try {
            LogHelper.printDebug(() -> "initializing voting button");
            RelativeLayout controlsLayout = (RelativeLayout) viewStub;
            String buttonResourceName = "sb_voting_button";
            ImageView imageView = controlsLayout.findViewById(getResourceIdentifier(buttonResourceName, "id"));
            if (imageView == null) {
                LogHelper.printException(() -> "Couldn't find imageView with \"" + buttonResourceName + "\"");
                return;
            }
            imageView.setOnClickListener(v -> {
                SponsorBlockUtils.onVotingClicked(v.getContext());
            });
            buttonReference = new WeakReference<>(imageView);

            // Animations
            if (fadeIn == null) {
                fadeIn = ReVancedUtils.getResourceAnimation("fade_in");
                fadeIn.setDuration(ReVancedUtils.getResourceInteger("fade_duration_fast"));
                fadeOut = ReVancedUtils.getResourceAnimation("fade_out");
                fadeOut.setDuration(ReVancedUtils.getResourceInteger("fade_duration_scheduled"));
            }
            isShowing = true;
            changeVisibilityImmediate(false);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    /**
     * injection point
     */
    public static void changeVisibilityNegatedImmediate(boolean visible) {
        changeVisibility(!visible, true);
    }

    /**
     * injection point
     */
    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        try {
            if (isShowing == visible) return;
            isShowing = visible;

            ImageView iView = buttonReference.get();
            if (iView == null) return;

            if (visible) {
                iView.clearAnimation();
                if (!shouldBeShown()) {
                    return;
                }
                if (!immediate) {
                    iView.startAnimation(fadeIn);
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (!immediate) {
                    iView.startAnimation(fadeOut);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "changeVisibility failure", ex);
        }
    }

    private static boolean shouldBeShown() {
        return SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_VOTING_ENABLED.getBoolean()
                && SegmentPlaybackController.videoHasSegments() && !VideoInformation.isAtEndOfVideo();
    }

    public static void hide() {
        if (!isShowing) {
            return;
        }
        ReVancedUtils.verifyOnMainThread();
        View v = buttonReference.get();
        if (v == null) {
            LogHelper.printDebug(() -> "Cannot hide voting button (value is null)");
            return;
        }
        v.setVisibility(View.GONE);
        isShowing = false;
    }
}
