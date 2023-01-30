package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getIdentifier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.SwipeHelper;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockView {

    private static RelativeLayout inlineSponsorOverlay;
    private static ViewGroup _youtubeOverlaysLayout;
    private static WeakReference<SkipSponsorButton> _skipSponsorButton = new WeakReference<>(null);
    private static WeakReference<NewSegmentLayout> _newSegmentLayout = new WeakReference<>(null);
    private static boolean shouldShowOnPlayerType = true;
    static {
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            SponsorBlockView.playerTypeChanged(type);
            return null;
        });
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

    public static void showSkipButton() {
        skipSponsorButtonVisibility(true);
    }

    public static void hideSkipButton() {
        skipSponsorButtonVisibility(false);
    }

    public static void showNewSegmentLayout() {
        newSegmentLayoutVisibility(true);
    }

    public static void hideNewSegmentLayout() {
        newSegmentLayoutVisibility(false);
    }

    static void playerTypeChanged(PlayerType playerType) {
        try {
            shouldShowOnPlayerType = (playerType == PlayerType.WATCH_WHILE_FULLSCREEN || playerType == PlayerType.WATCH_WHILE_MAXIMIZED);

            if (playerType == PlayerType.WATCH_WHILE_FULLSCREEN) {
                setSkipBtnMargins(true);
                setNewSegmentLayoutMargins(true);
                return;
            }

            setSkipBtnMargins(false);
            setNewSegmentLayoutMargins(false);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Player type changed caused a crash.", ex);
        }
    }

    private static void addView() {
        inlineSponsorOverlay = new RelativeLayout(ReVancedUtils.getContext());
        setLayoutParams(inlineSponsorOverlay);
        LayoutInflater.from(ReVancedUtils.getContext()).inflate(getIdentifier("inline_sponsor_overlay", "layout"), inlineSponsorOverlay);

        _youtubeOverlaysLayout.addView(inlineSponsorOverlay, _youtubeOverlaysLayout.getChildCount() - 2);

        SkipSponsorButton skipSponsorButton = inlineSponsorOverlay.findViewById(getIdentifier("skip_sponsor_button", "id"));
        _skipSponsorButton = new WeakReference<>(skipSponsorButton);

        NewSegmentLayout newSegmentView = inlineSponsorOverlay.findViewById(getIdentifier("new_segment_view", "id"));
        _newSegmentLayout = new WeakReference<>(newSegmentView);
    }

    private static void setLayoutParams(RelativeLayout relativeLayout) {
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
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
            LogHelper.printException(() -> "Unable to newSegmentLayoutVisibility");
            return;
        }

        visible &= shouldShowOnPlayerType;

        newSegmentLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        bringLayoutToFront();
    }

    private static void bringLayoutToFront() {
        checkLayout();
        inlineSponsorOverlay.bringToFront();
        inlineSponsorOverlay.requestLayout();
        inlineSponsorOverlay.invalidate();
    }

    // Edit: this is old code from Vanced and is not fully integrated into ReVanced
    private static void checkLayout() {
        if (inlineSponsorOverlay.getHeight() == 0) {
            ViewGroup watchLayout = SwipeHelper.nextGenWatchLayout;
            if (watchLayout == null) {
                LogHelper.printDebug(() -> "nextGenWatchLayout is null!");
                return;
            }
            View layout = watchLayout.findViewById(getIdentifier("player_overlays", "id"));

            if (layout == null) {
                LogHelper.printDebug(() -> "player_overlays was not found for SB");
                return;
            }

            initialize(layout);
            LogHelper.printDebug(() -> "player_overlays refreshed for SB");
        }
    }
}
