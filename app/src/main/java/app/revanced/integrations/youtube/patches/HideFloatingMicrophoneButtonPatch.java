package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.SettingsEnum;

public final class HideFloatingMicrophoneButtonPatch {
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return SettingsEnum.HIDE_FLOATING_MICROPHONE_BUTTON.getBoolean() || original;
    }
}
