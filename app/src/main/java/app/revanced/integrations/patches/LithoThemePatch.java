package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.ReVancedUtils.context;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    // color constants used in relation with litho components
    private static final int[] WHITECONSTANTS = {
        -1, // comments chip background
        -394759, // music related results panel background
        -83886081, // video chapters list background
    };
    private static final int SOLIDWHITE = getColor("yt_white1");

    private static final int[] DARKCONSTANTS = {
        -14145496, // explore drawer background
        -14606047, // comments chip background
        -15198184, // music related results panel background
        -15790321, // comments chip background (new layout)
        -98492127 // video chapters list background
    };
    private static final int SOLIDBLACK = getColor("yt_black1");

    // Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        if (ThemeHelper.isDarkTheme()) {
            if (anyEquals(originalValue, DARKCONSTANTS))
                return SOLIDWHITE;
        } else if (anyEquals(originalValue, WHITECONSTANTS))
            return SOLIDBLACK;

        return originalValue;
    }

    private static int getColor(String colorName) {
        return context.getColor(
            context.getResources().getIdentifier(
                colorName,
                "color",
                context.getPackageName()
            )
        );
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;
        return false;
    }
}
