package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return Setting.SEEKBAR_TAPPING.getBoolean();
    }
}
