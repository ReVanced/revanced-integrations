package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HidePlayerButtonsPatch {

    private static final boolean HIDE_PLAYER_BUTTONS_ENABLED = Settings.HIDE_PLAYER_BUTTONS.get();

    private static final int PREVIOUS_BUTTON_RESOURCE_ID =
            Utils.getResourceIdentifier("player_control_previous_button", "id");

    private static final int NEXT_BUTTON_RESOURCE_ID =
            Utils.getResourceIdentifier("player_control_next_button", "id");

    /**
     * Injection point.
     */
    public static void hidePreviousNextButtons(View parentView) {
        if (!HIDE_PLAYER_BUTTONS_ENABLED) {
            return;
        }

        hideButton(parentView, PREVIOUS_BUTTON_RESOURCE_ID);
        hideButton(parentView, NEXT_BUTTON_RESOURCE_ID);
    }

    private static void hideButton(View parentView, int resourceId) {
        try {
            View nextPreviousButton = parentView.findViewById(resourceId);

            if (nextPreviousButton == null) {
                Logger.printDebug(() -> "Could not find previous/next button");
            } else {
                Logger.printDebug(() -> "Hiding previous/next button");
                Utils.hideViewBy0dpUnderCondition(true, nextPreviousButton);

                // Button is no longer visible, but the click listener needs to be cleared otherwise
                // the button can still be pressed even though it's 0dp.
                //
                // The listener is added after this hook in the same target method.
                // To keep thing simple, the listener can be cleared on a deferred main thread call.
                Utils.runOnMainThread(() -> nextPreviousButton.setOnClickListener(null));
            }
        } catch (Exception ex) {
            Logger.printException(() -> "hideButton failure", ex);
        }
    }

}
