package app.revanced.integrations.patches;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.shared.VideoState;

@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Injection point.
     */
    public static void setPlayerType(@Nullable Enum<?> type) {
        if (type == null) return;

        PlayerType.setFromString(type.name());
    }

    /**
     * Injection point.
     */
    public static void setVideoState(@Nullable Enum<?> youTubeVideoState) {
        if (youTubeVideoState == null) return;

        VideoState.setFromString(youTubeVideoState.name());
    }
}
