package app.revanced.tiktok.speed;

import app.revanced.tiktok.settings.SettingsEnum;

public class SpeedPatch {
    public static void saveDefaultSpeed(float newSpeed) {
        SettingsEnum.SPEED_SAVED_SPEED.saveValue(newSpeed);
    }

    public static float getDefaultSpeed() {
        return SettingsEnum.SPEED_SAVED_SPEED.getFloat();
    }
}
