package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;

public class CustomSpeedDialogButton extends BottomControlButton {
    @Nullable
    private static CustomSpeedDialogButton instance;

    public CustomSpeedDialogButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "revanced_custom_playback_speed_dialog_button",
                Settings.CUSTOM_SPEED_DIALOG_BUTTON,
                view -> CustomPlaybackSpeedPatch.showOldPlaybackSpeedMenu(),
                null
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new CustomSpeedDialogButton((ViewGroup) view);
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }
}