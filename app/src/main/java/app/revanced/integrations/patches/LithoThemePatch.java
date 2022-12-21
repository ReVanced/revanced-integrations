package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.ReVancedUtils.getContext;

import android.content.Context;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    // color constants used in relation with litho components
    private static final int[] WHITECONSTANTS = {
        -1, // comments chip background
        -394759, // music related results panel background
        -83886081, // video chapters list background
    };
    private static int SOLIDWHITE = 0;

    private static final int[] DARKCONSTANTS = {
        -14145496, // explore drawer background
        -14606047, // comments chip background
        -15198184, // music related results panel background
        -15790321, // comments chip background (new layout)
        -98492127 // video chapters list background
    };
    private static int SOLIDBLACK = 0;

    // Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        if (ThemeHelper.isDarkTheme()) {
            if (anyEquals(originalValue, DARKCONSTANTS)) {
                if (SOLIDBLACK == 0)
                    SOLIDBLACK = getColor("yt_black1");

                return SOLIDBLACK;
            }
        } else if (anyEquals(originalValue, WHITECONSTANTS)) {
            if (SOLIDWHITE == 0)
                SOLIDWHITE = getColor("yt_white1");

            return SOLIDWHITE;
        }

        return originalValue;
    }

    private static int getColor(String colorName) {
        Context context = getContext();

        return context != null
                ?
                context.getColor(
                    context.getResources().getIdentifier(
                        colorName,
                        "color",
                        context.getPackageName()
                    )
                )
                :
                0;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;
        return false;
    }
}
