package app.revanced.music.utils;

import android.app.Activity;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(Object value) {
        final int newOrdinalValue = ((Enum) value).ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            LogHelper.printDebug(() -> "Theme value: " + newOrdinalValue);
        }
    }

    public static boolean isDarkTheme() {
        return true; // YT Music only has dark theme
    }

    public static void setActivityTheme(Activity activity) {
        final var theme = "Theme.YouTubeMusic";
        activity.setTheme(ReVancedUtils.getResourceIdentifier(theme, "style"));
    }
}
