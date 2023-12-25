package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public final class HideFloatingMicrophoneButtonPatch {
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return Setting.HIDE_FLOATING_MICROPHONE_BUTTON.getBoolean() || original;
    }
}
