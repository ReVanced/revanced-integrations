package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getIdentifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockViewController {
    private static RelativeLayout inlineSponsorOverlay;
    private static ViewGroup _youtubeOverlaysLayout;
    private static WeakReference<SkipSponsorButton> _skipSponsorButton = new WeakReference<>(null);
    private static WeakReference<NewSegmentLayout> _newSegmentLayout = new WeakReference<>(null);
    private static boolean shouldShowOnPlayerType = true;
    @Nullable
    private static SponsorSegment skipSegment;

    static {
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            playerTypeChanged(type);
            return null;
        });
    }

    public static Context getOverLaysViewGroupContext() {
        if (_youtubeOverlaysLayout == null) {
            return null;
        }
        return _youtubeOverlaysLayout.getContext();
    }

    public static void initialize(Object viewGroup) {
        try {
            LogHelper.printDebug(() -> "initializing");

            _youtubeOverlaysLayout = (ViewGroup) viewGroup;

            addView();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Unable to set ViewGroup", ex);
        }
    }

    public static void showSkipButton(@NonNull SponsorSegment info) {
        skipSegment = Objects.requireNonNull(info);
        updateSkipButton();
    }

    public static void hideSkipButton() {
        skipSegment = null;
        updateSkipButton();
    }

    private static void updateSkipButton() {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) {
            return;
        }
        if (skipSegment == null) {
            skipSponsorButtonVisibility(false);
        } else {
            final boolean layoutNeedsUpdating = skipSponsorButton.updateSkipButtonText(skipSegment);
            if (layoutNeedsUpdating) {
                bringLayoutToFront();
            }
            skipSponsorButtonVisibility(true);
        }
    }

    public static void showNewSegmentLayout() {
        newSegmentLayoutVisibility(true);
    }

    public static void hideNewSegmentLayout() {
        newSegmentLayoutVisibility(false);
    }

    public static void toggleNewSegmentLayout() {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) {
            LogHelper.printDebug(() -> "Unable to newSegmentLayoutVisibility");
            return;
        }
        newSegmentLayoutVisibility(newSegmentLayout.getVisibility() == View.VISIBLE ? false : true);
    }

    private static void playerTypeChanged(PlayerType playerType) {
        try {
            final boolean isWatchFullScreen = playerType == PlayerType.WATCH_WHILE_FULLSCREEN;
            shouldShowOnPlayerType = (isWatchFullScreen || playerType == PlayerType.WATCH_WHILE_MAXIMIZED);

            setSkipBtnMargins(isWatchFullScreen);
            setNewSegmentLayoutMargins(isWatchFullScreen);
            updateSkipButton();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Player type changed caused a crash.", ex);
        }
    }

    private static void addView() {
        inlineSponsorOverlay = new RelativeLayout(ReVancedUtils.getContext());
        inlineSponsorOverlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        LayoutInflater.from(ReVancedUtils.getContext()).inflate(getIdentifier("inline_sponsor_overlay", "layout"), inlineSponsorOverlay);

        _youtubeOverlaysLayout.addView(inlineSponsorOverlay, _youtubeOverlaysLayout.getChildCount() - 2);

        SkipSponsorButton skipSponsorButton = Objects.requireNonNull(inlineSponsorOverlay.findViewById(getIdentifier("sb_skip_sponsor_button", "id")));
        _skipSponsorButton = new WeakReference<>(skipSponsorButton);

        NewSegmentLayout newSegmentView = Objects.requireNonNull(inlineSponsorOverlay.findViewById(getIdentifier("sb_new_segment_view", "id")));
        _newSegmentLayout = new WeakReference<>(newSegmentView);
    }

    private static void setSkipBtnMargins(boolean fullScreen) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) {
            LogHelper.printException(() -> "Unable to setSkipBtnMargins");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) skipSponsorButton.getLayoutParams();
        if (params == null) {
            LogHelper.printException(() -> "Unable to setSkipBtnMargins");
            return;
        }
        params.bottomMargin = fullScreen ? skipSponsorButton.ctaBottomMargin : skipSponsorButton.defaultBottomMargin;
        skipSponsorButton.setLayoutParams(params);
    }

    private static void skipSponsorButtonVisibility(boolean visible) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) {
            LogHelper.printException(() -> "Unable to skipSponsorButtonVisibility");
            return;
        }

        visible &= shouldShowOnPlayerType;

        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (skipSponsorButton.getVisibility() != desiredVisibility) {
            skipSponsorButton.setVisibility(desiredVisibility);
            if (visible) {
                bringLayoutToFront();
            }
        }
    }

    private static void setNewSegmentLayoutMargins(boolean fullScreen) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) {
            LogHelper.printException(() -> "Unable to setNewSegmentLayoutMargins");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newSegmentLayout.getLayoutParams();
        if (params == null) {
            LogHelper.printException(() -> "Unable to setNewSegmentLayoutMargins");
            return;
        }
        params.bottomMargin = fullScreen ? newSegmentLayout.ctaBottomMargin : newSegmentLayout.defaultBottomMargin;
        newSegmentLayout.setLayoutParams(params);
    }

    private static void newSegmentLayoutVisibility(boolean visible) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) {
            LogHelper.printDebug(() -> "Unable to newSegmentLayoutVisibility");
            return;
        }

        visible &= shouldShowOnPlayerType;

        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (newSegmentLayout.getVisibility() != desiredVisibility) {
            newSegmentLayout.setVisibility(desiredVisibility);
            if (visible) {
                bringLayoutToFront();
            }
        }
    }

    private static void bringLayoutToFront() {
        // needed to keep skip button overtop end screen cards
        inlineSponsorOverlay.bringToFront();
        inlineSponsorOverlay.requestLayout();
        inlineSponsorOverlay.invalidate();
    }
}
