package app.revanced.integrations.patches;

import android.util.Log;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    // color constants used in relation with litho components
    public static class STANDARDPLAYER {
        public static final int[] WHITECONSTANTS = {
            -1, // comments chip background
            -394759, // music related results panel background
            -83886081, // video chapters list background
        };
        public static final int[] DARKCONSTANTS = {
            -14145496, // explore drawer background
            -14606047, // comments chip background
            -15198184, // music related results panel background
            -15790321, // comments chip background (new layout)
            -98492127 // video chapters list background
        };
    }

    public static class SHORTSPLAYER {
        public static final int[] DARKCONSTANTS = {
            -14606047, // shorts comment box background
        };
    }

    // check if the player is changed
    public static boolean isStandardPlayer = false;

    // Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        var isDarkTheme = ThemeHelper.isDarkTheme();

        if (isStandardPlayer) {
            if ((isDarkTheme && anyEquals(originalValue, STANDARDPLAYER.DARKCONSTANTS)) ||
                (!isDarkTheme && anyEquals(originalValue, STANDARDPLAYER.WHITECONSTANTS)))
                    return 0;
        } else {
            if ((isDarkTheme && anyEquals(originalValue, SHORTSPLAYER.DARKCONSTANTS)))
                return -16777215;
        }
        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;
        return false;
    }
}
