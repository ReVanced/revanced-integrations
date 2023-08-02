package app.revanced.music.patches;

import app.revanced.music.settings.SettingsEnum;

public class PermanentRepeatTogglePatch {
    //Used by app.revanced.patches.music.layout.permanentrepeattoggle.patch.PermanentRepeatTogglePatch
    public static boolean permanentRepeatEnabled() {
        return SettingsEnum.PERMANENT_REPEAT.getBoolean();
    }
}
