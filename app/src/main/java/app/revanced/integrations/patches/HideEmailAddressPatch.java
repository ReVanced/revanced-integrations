package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideEmailAddressPatch {
    //Used by app.revanced.patches.youtube.layout.personalinformation.patch.HideEmailAddressPatch
    public static int hideEmailAddress(int originalValue) {
        if (SettingsEnum.HIDE_EMAIL_ADDRESS.getBoolean())
            return 8;
        return originalValue;
    }
}
