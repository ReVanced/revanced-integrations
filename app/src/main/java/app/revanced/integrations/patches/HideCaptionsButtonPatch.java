package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideCaptionsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.hidecaptionsbutton.patch.HideCaptionsButtonPatch
    public static boolean hideCaptionsButton() {
        return SettingsEnum.HIDE_CAPTIONS_BUTTON.getBoolean();
    }
}
