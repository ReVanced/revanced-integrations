package app.revanced.integrations.patches;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    // constants used in relation with litho components
    private static final int[] CONSTANTS = {
            -14606047, // comments chip background
            -15790321, // comments chip background (new layout)
            -98492127 // video chapters list backround
    };

    // Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        if (originalValue == -1 || (anyEquals(originalValue, CONSTANTS)) && ThemeHelper.isDarkTheme())
            return 0;
        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;
        return false;
    }
}
