package app.revanced.integrations.patches.theme;

import android.graphics.Color;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public final class ThemePatch {
    private static final int ORIGINAL_SEEKBAR_CLICKED_COLOR = -65536;

    private static void resetSeekbarColor() {
        ReVancedUtils.showToastShort("Invalid seekbar color value. Using default value.");
        SettingsEnum.SEEKBAR_COLOR.saveValue(SettingsEnum.SEEKBAR_COLOR.defaultValue);
    }

    /**
     * Injection point.
     */
    public static int getSeekbarClickedColorValue(final int currentColorValue) {
        // YouTube uses a specific color when the seekbar is clicked. Override in that case.
        return currentColorValue == ORIGINAL_SEEKBAR_CLICKED_COLOR ? getSeekbarColorValue(currentColorValue) : currentColorValue;
    }

    public static int getSeekbarColorValue(final int currentColorValue) {
        try {
            String seekbarColor = SettingsEnum.SEEKBAR_COLOR.getString();
            return seekbarColor.equals("") ? currentColorValue : Color.parseColor(seekbarColor);
        } catch (Exception exception) {
            resetSeekbarColor();
            return getSeekbarColorValue(currentColorValue);
        }
    }
}
