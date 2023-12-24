package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public class DisableAutoCaptionsPatch {

    /**
     * Used by injected code. Do not delete.
     */
    public static boolean captionsButtonDisabled;

    public static boolean autoCaptionsEnabled() {
        return SettingsEnum.AUTO_CAPTIONS.getBoolean();
    }

}
