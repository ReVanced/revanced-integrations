package app.revanced.integrations.patches;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    //Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        // Prepare newValue field, which will eventually overwrite originalValue by a return.
        int newValue = 0;

        // Check if app theme is white or dark. This will prevents the newValue return
        // after a satisfied condition, regardless on both themes, to avoid unwanted color changes.
        if (!ThemeHelper.isDarkTheme()) {
            // Check if originalValue is equal to:
            // "-1" (the color value of the 'white comments filter bar', shown on both
            // Old and New UI Layout of YouTube) and return newValue, if condition is satisfied.
            if (originalValue == -1) {
                return newValue;
            }
        }
        else
        {
            // Check if originalValue is equal to:
            // "-14606047" (the value of "dark comments filter bar", shown on the Old UI Layout of YouTube)
            // "-15790321" (the value of "dark comments filter bar", shown on the New UI Layout of YouTube)
            // "-98492127" (the value of "dark video chapters list"),
            // and return newValue, if one of below conditions is satisfied.
            if (originalValue == -14606047 ||
                originalValue == -15790321 ||
                originalValue == -98492127) {
                    return newValue;
            }
        }

        //Return originalValue, if none of the above conditions are satisfied
        return originalValue;
    }
}
