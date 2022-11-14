package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.utils.ResourceUtils.findView;
import static app.revanced.integrations.utils.ResourceUtils.identifier;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ResourceType;

@SuppressLint("StaticFieldLeak")
public class SponsorBlockView {
    static RelativeLayout inlineSponsorOverlay;
    static ViewGroup _youtubeOverlaysLayout;
    static WeakReference<SkipSponsorButton> _skipSponsorButton = new WeakReference<>(null);
    static WeakReference<NewSegmentLayout> _newSegmentLayout = new WeakReference<>(null);
    static boolean shouldShowOnPlayerType = true;

    public static void initialize(Object viewGroup) {
        try {
            _youtubeOverlaysLayout = (ViewGroup) viewGroup;
            addView();
        } catch (Exception ex) {
            LogHelper.printException(SponsorBlockView.class, "Unable to set ViewGroup", ex);
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

    public static void playerTypeChanged(PlayerType playerType) {
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
            LogHelper.printException(SponsorBlockView.class, "Player type changed caused a crash.", ex);
        }
    }

    private static void addView() {
        var context = ReVancedUtils.context();
        inlineSponsorOverlay = new RelativeLayout(context);
        setLayoutParams(inlineSponsorOverlay);
        LayoutInflater.from(context).inflate(
                identifier("inline_sponsor_overlay", ResourceType.LAYOUT),
                inlineSponsorOverlay
        );

        _youtubeOverlaysLayout.addView(inlineSponsorOverlay, _youtubeOverlaysLayout.getChildCount() - 2);

        SkipSponsorButton skipSponsorButton = inlineSponsorOverlay.findViewById(identifier("skip_sponsor_button", ResourceType.ID));
        _skipSponsorButton = new WeakReference<>(skipSponsorButton);

        NewSegmentLayout newSegmentView = inlineSponsorOverlay.findViewById(identifier("new_segment_view", ResourceType.ID));
        _newSegmentLayout = new WeakReference<>(newSegmentView);
    }

    private static void setLayoutParams(RelativeLayout relativeLayout) {
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private static void setSkipBtnMargins(boolean fullScreen) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) return;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) skipSponsorButton.getLayoutParams();
        if (params == null) return;
        params.bottomMargin = fullScreen ? skipSponsorButton.ctaBottomMargin : skipSponsorButton.defaultBottomMargin;
        skipSponsorButton.setLayoutParams(params);
    }

    private static void skipSponsorButtonVisibility(boolean visible) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) return;
        visible &= shouldShowOnPlayerType;
        skipSponsorButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        bringLayoutToFront();
    }

    private static void setNewSegmentLayoutMargins(boolean fullScreen) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) return;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newSegmentLayout.getLayoutParams();
        if (params == null) return;
        params.bottomMargin = fullScreen ? newSegmentLayout.ctaBottomMargin : newSegmentLayout.defaultBottomMargin;
        newSegmentLayout.setLayoutParams(params);
    }

    private static void newSegmentLayoutVisibility(boolean visible) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) return;
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

    private static void checkLayout() {
        if (inlineSponsorOverlay.getHeight() == 0) {
            // FIXME: nextGenWatchLayout is never being set by the patches.
            // Decompile YT Vanced and modify the swipe-controls patch to set nextGenWatchLayout.
            // ViewGroup watchLayout = SwipeHelper.nextGenWatchLayout;
            ViewGroup watchLayout = null;
            //noinspection ConstantConditions
            if (watchLayout == null) {
                LogHelper.debug(SponsorBlockView.class, "nextGenWatchLayout is null!");
                return;
            }

            initialize(findView(SponsorBlockView.class, watchLayout, "player_overlays"));
        }
    }
}
