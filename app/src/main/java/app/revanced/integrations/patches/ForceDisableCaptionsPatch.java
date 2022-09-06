package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class ForceDisableCaptionsPatch {

    public static boolean captionsButtonDisabled;

    public static boolean autoCaptionsEnabled() {
        return SettingsEnum.CAPTIONS_ENABLED.getBoolean();
    }

}
