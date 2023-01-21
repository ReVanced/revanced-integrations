package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class OpenLinksExternallyPatch {
    public static String enableExternalBrowser(String original) {
        /** 
            * "original" is 'android.support.customtabs.action.CustomTabsService' is a string constant 
                that represents the action for the service that handles Custom Tabs in the Android Support Library. 
                Custom Tabs is a feature that allows developers to open web content in a Chrome-based in-app browser, 
                rather than in the default browser.
                if we set it to empty string, it will open the link in the default browser.
            */
        if (SettingsEnum.ENABLE_EXTERNAL_BROWSER.getBoolean()) original = "";
        return original;
    }
}
