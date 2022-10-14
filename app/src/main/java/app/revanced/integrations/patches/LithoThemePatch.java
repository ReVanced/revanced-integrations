package app.revanced.integrations.patches;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    //Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int value) {
        int newValue = 0;

        if (!ThemeHelper.isDarkTheme()) {
            if (value == -1) {
                return newValue;
            }
        }
        else
        {
            if (value == -14606047 ||
                value == -15790321 ||
                value == -98492127) {
                return newValue;
            }
        }

        return value;
    }
}
