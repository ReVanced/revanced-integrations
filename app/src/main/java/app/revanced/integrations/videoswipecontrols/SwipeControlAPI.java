package app.revanced.integrations.videoswipecontrols;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.NewSegmentHelperLayout;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SwipeHelper;

public class SwipeControlAPI {

    private static FensterGestureController fensterGestureController;

    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        fensterGestureController = new FensterGestureController();
        fensterGestureController.setFensterEventsListener(new XFenster(context, viewGroup), context, viewConfiguration);
        LogHelper.debug("Settings", "XFenster initialized");
    }

    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        if (fensterGestureController == null) {
            LogHelper.debug("Settings", "fensterGestureController is null");
            return false;
        } else if (motionEvent == null) {
            LogHelper.debug("Settings", "motionEvent is null");
            return false;
        } else if (!SwipeHelper.IsControlsShown()) {
            return fensterGestureController.onTouchEvent(motionEvent);
        } else {
            LogHelper.debug("Settings", "skipping onTouchEvent dispatching because controls are shown.");
            return false;
        }
    }

    public static void PlayerTypeChanged(PlayerType playerType) {
        LogHelper.debug("XDebug", playerType.toString());
        if (ReVancedUtils.getPlayerType() != playerType) {
            if (playerType == PlayerType.WATCH_WHILE_FULLSCREEN) {
                EnableSwipeControl();
            } else {
                DisableSwipeControl();
            }
            if (playerType == PlayerType.WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || playerType == PlayerType.WATCH_WHILE_MINIMIZED || playerType == PlayerType.WATCH_WHILE_PICTURE_IN_PICTURE) {
                NewSegmentHelperLayout.hide();
            }
            SponsorBlockView.playerTypeChanged(playerType);
            SponsorBlockUtils.playerTypeChanged(playerType);
        }
        ReVancedUtils.setPlayerType(playerType);
    }

    private static void EnableSwipeControl() {
        if (SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() || SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean()) {
            FensterGestureController fensterGestureController2 = fensterGestureController;
            fensterGestureController2.TouchesEnabled = true;
            ((XFenster) fensterGestureController2.listener).enable(SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean(), SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean());
        }
    }

    private static void DisableSwipeControl() {
        FensterGestureController fensterGestureController2 = fensterGestureController;
        fensterGestureController2.TouchesEnabled = false;
        ((XFenster) fensterGestureController2.listener).disable();
    }

}
