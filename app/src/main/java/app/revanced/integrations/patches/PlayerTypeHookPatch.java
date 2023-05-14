package app.revanced.integrations.patches;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.shared.VideoState;
import app.revanced.integrations.utils.LogHelper;

@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Injection point.
     */
    public static void YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(@Nullable Object type) {
        if (type == null) return;

        final PlayerType newType = PlayerType.safeParseFromString(type.toString());
        if (newType == null) {
            LogHelper.printException(() -> "Unknown PlayerType encountered: " + type);
        } else {
            PlayerType.setCurrent(newType);
            LogHelper.printDebug(() -> "PlayerType was updated to: " + newType);
        }
    }

    /**
     * Injection point.
     */
    public static void setVideoState(Enum youTubeVideoState) {
        if (youTubeVideoState == null) return;

        VideoState.setFromString(youTubeVideoState.name());
    }
}
