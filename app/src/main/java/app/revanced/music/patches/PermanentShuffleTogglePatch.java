package app.revanced.music.patches;

import app.revanced.music.settings.SettingsEnum;

public class PermanentShuffleTogglePatch {
    //Used by app.revanced.patches.music.layout.permanentshuffletoggle.patch.PermanentShuffleTogglePatch
    public static boolean permanentShuffleEnabled() {
        return SettingsEnum.PERMANENT_SHUFFLE.getBoolean();
    }
}
