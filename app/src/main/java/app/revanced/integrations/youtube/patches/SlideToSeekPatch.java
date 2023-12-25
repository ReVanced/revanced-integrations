package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !Settings.SLIDE_TO_SEEK.getBoolean();
    }
}
