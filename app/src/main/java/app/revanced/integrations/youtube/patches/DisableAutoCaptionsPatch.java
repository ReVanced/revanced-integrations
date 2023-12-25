package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class DisableAutoCaptionsPatch {

    /**
     * Used by injected code. Do not delete.
     */
    public static boolean captionsButtonDisabled;

    public static boolean autoCaptionsEnabled() {
        return Settings.AUTO_CAPTIONS.getBoolean();
    }

}
