package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class YoodlesPatch {

    /**
     * Injection point.
     */
    public static boolean hideYoodles() {
        return Settings.HIDE_YOODLES.get();
    }
}
