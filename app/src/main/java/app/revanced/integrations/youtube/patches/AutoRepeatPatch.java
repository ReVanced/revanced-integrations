package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class AutoRepeatPatch {
    //Used by app.revanced.patches.youtube.layout.autorepeat.patch.AutoRepeatPatch
    public static boolean shouldAutoRepeat() {
        return Setting.AUTO_REPEAT.getBoolean();
    }
}
