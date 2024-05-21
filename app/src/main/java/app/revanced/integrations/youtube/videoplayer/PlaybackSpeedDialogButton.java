package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;

public class PlaybackSpeedDialogButton extends BottomControlButton {
    @Nullable
    private static PlaybackSpeedDialogButton instance;

    public PlaybackSpeedDialogButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "revanced_playback_speed_dialog_button",
                Settings.PLAYBACK_SPEED_DIALOG_BUTTON,
                view -> CustomPlaybackSpeedPatch.showOldPlaybackSpeedMenu(),
                null
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new PlaybackSpeedDialogButton((ViewGroup) view);
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