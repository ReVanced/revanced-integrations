package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class DisableAutoCaptionsPatch {

    /**
     * Used by injected code. Do not delete.
     */
    public static boolean captionsButtonDisabled;

    public static boolean autoCaptionsEnabled() {
        return Settings.AUTO_CAPTIONS.get()
                // Do not use auto captions for Shorts.
                && !PlayerType.getCurrent().isNoneHiddenOrSlidingMinimized();
    }

}
