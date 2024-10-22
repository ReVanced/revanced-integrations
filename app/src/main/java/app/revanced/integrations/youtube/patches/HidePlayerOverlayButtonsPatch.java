package app.revanced.integrations.youtube.patches;

import android.view.View;
import android.widget.ImageView;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HidePlayerOverlayButtonsPatch {

    private static final boolean HIDE_AUTOPLAY_BUTTON_ENABLED = Settings.HIDE_AUTOPLAY_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean hideAutoPlayButton() {
        return HIDE_AUTOPLAY_BUTTON_ENABLED;
    }

    /**
     * Injection point.
     */
    public static int getCastButtonOverrideV2(int original) {
        return Settings.HIDE_CAST_BUTTON.get() ? View.GONE : original;
    }

    /**
     * Injection point.
     */
    public static void hideCaptionsButton(ImageView imageView) {
        imageView.setVisibility(Settings.HIDE_CAPTIONS_BUTTON.get() ? ImageView.GONE : ImageView.VISIBLE);
    }

    private static final boolean HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS_ENABLED
            = Settings.HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS.get();

    private static final int PLAYER_CONTROL_PREVIOUS_BUTTON_TOUCH_AREA_ID =
            Utils.getResourceIdentifier("player_control_previous_button_touch_area", "id");

    private static final int PLAYER_CONTROL_NEXT_BUTTON_TOUCH_AREA_ID =
            Utils.getResourceIdentifier("player_control_next_button_touch_area", "id");

    /**
     * Injection point.
     */
    public static void hidePreviousNextButtons(View parentView) {
        if (!HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS_ENABLED) {
            return;
        }

        // Must use a deferred call to main thread to hide the button.
        // Otherwise the layout crashes if set to hidden now.
        Utils.runOnMainThread(() -> {
            hideView(parentView, PLAYER_CONTROL_PREVIOUS_BUTTON_TOUCH_AREA_ID);
            hideView(parentView, PLAYER_CONTROL_NEXT_BUTTON_TOUCH_AREA_ID);
        });
    }

    private static void hideView(View parentView, int resourceId) {
        View nextPreviousButton = parentView.findViewById(resourceId);

        if (nextPreviousButton == null) {
            Logger.printException(() -> "Could not find player previous/next button");
            return;
        }

        Logger.printDebug(() -> "Hiding previous/next button");
        Utils.hideViewByRemovingFromParentUnderCondition(true, nextPreviousButton);
    }
}
