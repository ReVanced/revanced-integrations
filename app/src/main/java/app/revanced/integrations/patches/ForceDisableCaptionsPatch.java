package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class ForceDisableCaptionsPatch {

    //ToDo: Write Patch for it
    public static boolean captionsEnabled() {
        return SettingsEnum.CAPTIONS_ENABLED.getBoolean();
    }

}
