package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !Setting.SLIDE_TO_SEEK.getBoolean();
    }
}
