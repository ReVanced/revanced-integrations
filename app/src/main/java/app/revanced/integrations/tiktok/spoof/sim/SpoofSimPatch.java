package app.revanced.integrations.tiktok.spoof.sim;

import app.revanced.integrations.tiktok.settings.Settings;

public class SpoofSimPatch {
    public static boolean isEnable() {
        return Settings.SIM_SPOOF.getBoolean();
    }
    public static String getCountryIso(String value) {
        if (isEnable()) {
            return Settings.SIM_SPOOF_ISO.getString();
        } else {
            return value;
        }

    }
    public static String getOperator(String value) {
        if (isEnable()) {
            return Settings.SIMSPOOF_MCCMNC.getString();
        } else {
            return value;
        }
    }
    public static String getOperatorName(String value) {
        if (isEnable()) {
            return Settings.SIMSPOOF_OP_NAME.getString();
        } else {
            return value;
        }
    }
}
