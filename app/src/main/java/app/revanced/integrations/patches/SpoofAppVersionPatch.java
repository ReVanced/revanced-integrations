package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class SpoofAppVersionPatch {

    public static String getYouTubeVersionOverride(String version) {
        if (SettingsEnum.SPOOF_OLD_YOUTUBE_APP_VERSION.getBoolean()){
            // override with the most recent version that does not show the new UI player layout.
            // If the new ui shows up for some users, then change this to an older version (such as 17.29.34)
            return "17.30.34";
        }
        return version; // return the existing version string
    }
}
