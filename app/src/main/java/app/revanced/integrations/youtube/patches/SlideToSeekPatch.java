package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !SettingsEnum.SLIDE_TO_SEEK.getBoolean();
    }
}
