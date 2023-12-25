package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return Settings.SEEKBAR_TAPPING.getBoolean();
    }
}
