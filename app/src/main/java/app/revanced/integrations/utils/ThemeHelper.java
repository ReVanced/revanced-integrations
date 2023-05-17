package app.revanced.integrations.utils;

import android.app.Activity;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(int value) {
        if (themeValue != value) {
            themeValue = value;
            LogHelper.printDebug(() -> "Theme value: " + themeValue);
        }
    }

    /**
     * Injection point.
     */
    public static void setTheme(Object value) {
        setTheme(((Enum) value).ordinal());
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

    public static void setActivityTheme(Activity activity) {
        final var theme = isDarkTheme()
                ? "Theme.YouTube.Settings.Dark"
                : "Theme.YouTube.Settings";
        activity.setTheme(ReVancedUtils.getResourceIdentifier(theme, "style"));
    }

}
