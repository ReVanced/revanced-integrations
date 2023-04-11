package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdentifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockViewController {
    private static WeakReference<RelativeLayout> inlineSponsorOverlayRef = new WeakReference<>(null);
    private static WeakReference<ViewGroup> youtubeOverlaysLayoutRef = new WeakReference<>(null);
    private static WeakReference<SkipSponsorButton> skipHighlightButtonRef = new WeakReference<>(null);
    private static WeakReference<SkipSponsorButton> skipSponsorButtonRef = new WeakReference<>(null);
    private static WeakReference<NewSegmentLayout> newSegmentLayoutRef = new WeakReference<>(null);
    private static boolean canShowViewElements = true;
    @Nullable
    private static SponsorSegment skipHighlight;
    @Nullable
    private static SponsorSegment skipSegment;

    static {
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            playerTypeChanged(type);
            return null;
        });
    }

    public static Context getOverLaysViewGroupContext() {
        ViewGroup group = youtubeOverlaysLayoutRef.get();
        if (group == null) {
            return null;
        }
        return group.getContext();
    }

    /**
     * Injection point.
     */
    public static void initialize(Object obj) {
        try {
            LogHelper.printDebug(() -> "initializing");

            Context context = ReVancedUtils.getContext();
            RelativeLayout layout = new RelativeLayout(context);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            LayoutInflater.from(context).inflate(getResourceIdentifier("inline_sponsor_overlay", "layout"), layout);
            inlineSponsorOverlayRef = new WeakReference<>(layout);

            ViewGroup viewGroup = (ViewGroup) obj;
            viewGroup.addView(layout, viewGroup.getChildCount() - 2);
            youtubeOverlaysLayoutRef = new WeakReference<>(viewGroup);

            skipHighlightButtonRef = new WeakReference<>(
                    Objects.requireNonNull(layout.findViewById(getResourceIdentifier("sb_skip_highlight_button", "id"))));

            skipSponsorButtonRef = new WeakReference<>(
                    Objects.requireNonNull(layout.findViewById(getResourceIdentifier("sb_skip_sponsor_button", "id"))));

            newSegmentLayoutRef = new WeakReference<>(
                    Objects.requireNonNull(layout.findViewById(getResourceIdentifier("sb_new_segment_view", "id"))));
        } catch (Exception ex) {
            LogHelper.printException(() -> "initialize failure", ex);
        }
    }

    public static void hideAll() {
        hideSkipHighlightButton();
        hideSkipSegmentButton();
        hideNewSegmentLayout();
    }

    public static void showSkipHighlightButton(@NonNull SponsorSegment segment) {
        skipHighlight = Objects.requireNonNull(segment);
        updateSkipButton(skipHighlightButtonRef.get(), segment);
    }
    public static void showSkipSegmentButton(@NonNull SponsorSegment segment) {
        skipSegment = Objects.requireNonNull(segment);
        updateSkipButton(skipSponsorButtonRef.get(), segment);
    }

    public static void hideSkipHighlightButton() {
        skipHighlight = null;
        updateSkipButton(skipHighlightButtonRef.get(), null);
    }
    public static void hideSkipSegmentButton() {
        skipSegment = null;
        updateSkipButton(skipSponsorButtonRef.get(), null);
    }

    private static void updateSkipButton(SkipSponsorButton button, SponsorSegment segment) {
        if (button == null) {
            return;
        }
        if (segment == null) {
            setViewVisibility(button, false);
        } else {
            final boolean layoutNeedsUpdating = button.updateSkipButtonText(segment);
            if (layoutNeedsUpdating) {
                bringLayoutToFront();
            }
            setViewVisibility(button, true);
        }
    }

    public static void toggleNewSegmentLayoutVisibility() {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            LogHelper.printException(() -> "toggleNewSegmentLayoutVisibility failure");
            return;
        }
        setViewVisibility(newSegmentLayout, newSegmentLayout.getVisibility() == View.VISIBLE ? false : true);
    }

    public static void hideNewSegmentLayout() {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            return;
        }
        setViewVisibility(newSegmentLayout, false);
    }

    private static void playerTypeChanged(PlayerType playerType) {
        try {
            final boolean isWatchFullScreen = playerType == PlayerType.WATCH_WHILE_FULLSCREEN;
            canShowViewElements = (isWatchFullScreen || playerType == PlayerType.WATCH_WHILE_MAXIMIZED);
            setNewSegmentLayoutMargins(isWatchFullScreen);

            SkipSponsorButton skipSponsorButton = skipSponsorButtonRef.get();
            SkipSponsorButton skipHighlightButton = skipHighlightButtonRef.get();
            setSkipButtonMargins(skipSponsorButton, isWatchFullScreen);
            setSkipButtonMargins(skipHighlightButton, isWatchFullScreen);
            updateSkipButton(skipSponsorButton, skipSegment);
            updateSkipButton(skipHighlightButton, skipHighlight);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Player type changed error", ex);
        }
    }

    private static void setNewSegmentLayoutMargins(boolean fullScreen) {
        NewSegmentLayout layout = newSegmentLayoutRef.get();
        setLayoutMargins(layout, fullScreen, layout.defaultBottomMargin, layout.ctaBottomMargin);
    }
    private static void setSkipButtonMargins(SkipSponsorButton button, boolean fullScreen) {
        setLayoutMargins(button, fullScreen, button.defaultBottomMargin, button.ctaBottomMargin);
    }
    private static void setLayoutMargins(View view, boolean fullScreen, int defaultBottomMargin, int ctaBottomMargin) {
        if (view == null) {
            LogHelper.printException(() -> "setLayoutMargins failure (view is null)");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (params == null) {
            LogHelper.printException(() -> "Unable to setNewSegmentLayoutMargins (params are null)");
            return;
        }
        params.bottomMargin = fullScreen ? ctaBottomMargin : defaultBottomMargin;
        view.setLayoutParams(params);
    }

    private static void setViewVisibility(View view, boolean visible) {
        if (view == null) {
            LogHelper.printException(() -> "setViewVisibility failure: " + view);
            return;
        }

        visible &= canShowViewElements;

        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (view.getVisibility() != desiredVisibility) {
            view.setVisibility(desiredVisibility);
            if (visible) {
                bringLayoutToFront();
            }
        }
    }

    private static void bringLayoutToFront() {
        RelativeLayout layout = inlineSponsorOverlayRef.get();
        if (layout != null) {
            layout.bringToFront(); // needed to keep skip buttons overtop end screen cards

            // edit: this does not appear to be needed
//             layout.requestLayout();
//             layout.invalidate();
        }
    }

    /**
     * Injection point.
     */
    public static void endOfVideoReached() {
        try {
            LogHelper.printDebug(() -> "endOfVideoReached");
            // the buttons automatically set themselves to visible when appropriate,
            // but if buttons are showing when the end of the video is reached then they need
            // to be forcefully hidden
            if (!SettingsEnum.PREFERRED_AUTO_REPEAT.getBoolean()) {
                CreateSegmentButtonController.hide();
                VotingButtonController.hide();
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "endOfVideoReached failure", ex);
        }
    }
}
