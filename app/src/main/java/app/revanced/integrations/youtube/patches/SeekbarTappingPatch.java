package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return SettingsEnum.SEEKBAR_TAPPING.getBoolean();
    }
}
