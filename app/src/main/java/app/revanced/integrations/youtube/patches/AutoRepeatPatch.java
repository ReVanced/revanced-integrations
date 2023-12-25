package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

public class AutoRepeatPatch {
    //Used by app.revanced.patches.youtube.layout.autorepeat.patch.AutoRepeatPatch
    public static boolean shouldAutoRepeat() {
        return Settings.AUTO_REPEAT.getBoolean();
    }
}
