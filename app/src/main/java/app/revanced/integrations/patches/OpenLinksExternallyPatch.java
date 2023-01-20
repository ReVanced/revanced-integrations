package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class OpenLinksExternallyPatch {
    public static String enableExternalBrowser(String original) {
        if (SettingsEnum.ENABLE_EXTERNAL_BROWSER.getBoolean()) original = "";
        return original;
    }
}
