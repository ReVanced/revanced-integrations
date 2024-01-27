package app.revanced.integrations.youtube.sponsorblock.ui;

import static app.revanced.integrations.shared.Utils.getResourceIdentifier;

import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.videoplayer.BottomControlButton;

public class CreateSegmentButtonController {
    private static WeakReference<ImageView> buttonReference = new WeakReference<>(null);
    private static boolean isShowing;

    /**
     * injection point
     */
    public static void initialize(View youtubeControlsLayout) {
        try {
            Logger.printDebug(() -> "initializing new segment button");
            ImageView imageView = Objects.requireNonNull(youtubeControlsLayout.findViewById(
                    getResourceIdentifier("revanced_sb_create_segment_button", "id")));
            imageView.setVisibility(View.GONE);
            imageView.setOnClickListener(v -> {
                SponsorBlockViewController.toggleNewSegmentLayoutVisibility();
            });

            buttonReference = new WeakReference<>(imageView);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
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
                    iView.startAnimation(BottomControlButton.getButtonFadeIn());
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (!immediate) {
                    iView.startAnimation(BottomControlButton.getButtonFadeOut());
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeVisibility failure", ex);
        }
    }

    private static boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_CREATE_NEW_SEGMENT.get()
                && !VideoInformation.isAtEndOfVideo();
    }

    public static void hide() {
        if (!isShowing) {
            return;
        }
        Utils.verifyOnMainThread();
        View v = buttonReference.get();
        if (v == null) {
            return;
        }
        v.setVisibility(View.GONE);
        isShowing = false;
    }
}
