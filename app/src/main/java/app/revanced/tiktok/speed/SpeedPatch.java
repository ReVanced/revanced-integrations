package app.revanced.tiktok.speed;

import app.revanced.tiktok.settings.SettingsEnum;

public class SpeedPatch {
    public static void rememberPlaybackSpeed(float newSpeed) {
        SettingsEnum.REMEMBERED_SPEED.saveValue(newSpeed);
    }

    public static float getPlaybackSpeed() {
        return SettingsEnum.REMEMBERED_SPEED.getFloat();
    }
}
