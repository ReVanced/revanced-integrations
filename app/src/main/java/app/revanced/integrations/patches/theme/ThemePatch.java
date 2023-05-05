package app.revanced.integrations.patches.theme;

import android.graphics.Color;

import androidx.annotation.NonNull;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class ThemePatch {
    /**
     * Default color of seekbar.
     */
    private static final int ORIGINAL_SEEKBAR_CLICKED_COLOR = 0xFFFF0000;

    /**
     * Default YouTube seekbar color brightness.
     */
    private static final float ORIGINAL_SEEKBAR_CLICKED_COLOR_BRIGHTNESS;

    /**
     * Custom seekbar hue, saturation, and brightness values.
     */
    private static final float[] customSeekbarColorHSV = new float[3];

    static {
        float[] hsv = new float[3];
        Color.colorToHSV(ORIGINAL_SEEKBAR_CLICKED_COLOR, hsv);
        ORIGINAL_SEEKBAR_CLICKED_COLOR_BRIGHTNESS = hsv[2];

        setCustomSeekbarColorHSV(SettingsEnum.SEEKBAR_COLOR.getString());
    }

    private static void setCustomSeekbarColorHSV(@NonNull String colorString) {
        Color.colorToHSV(Color.parseColor(colorString), customSeekbarColorHSV);
    }

    private static void resetSeekbarColor() {
        ReVancedUtils.showToastShort("Invalid seekbar color value. Using default value.");
        String defaultSeekbarColor = (String) SettingsEnum.SEEKBAR_COLOR.defaultValue;
        SettingsEnum.SEEKBAR_COLOR.saveValue(defaultSeekbarColor);
        setCustomSeekbarColorHSV(defaultSeekbarColor);
    }

    /**
     * Injection point.
     */
    public static int getSeekbarClickedColorValue(final int colorValue) {
        // YouTube uses a specific color when the seekbar is clicked. Override in that case.
        return colorValue == ORIGINAL_SEEKBAR_CLICKED_COLOR
                ? getSeekbarColorValue(ORIGINAL_SEEKBAR_CLICKED_COLOR)
                : colorValue;
    }

    public static int getSeekbarColorValue(int originalColor) {
        try {
            if (SettingsEnum.SEEKBAR_COLOR.getObjectValue().equals(SettingsEnum.SEEKBAR_COLOR.defaultValue)) {
                return originalColor; // Nothing to do
            }
            final int originalAlpha = Color.alpha(originalColor);

            // The seekbar uses the same color but different brightness for different situations.
            float[] hsv = new float[3];
            Color.colorToHSV(originalColor, hsv);
            final float brightnessDifference = hsv[2] - ORIGINAL_SEEKBAR_CLICKED_COLOR_BRIGHTNESS;

            // Apply the saturation difference to the custom seekbar color.
            hsv[0] = customSeekbarColorHSV[0];
            hsv[1] = customSeekbarColorHSV[1];
            hsv[2] = Math.max(0, customSeekbarColorHSV[2] + brightnessDifference);

            final int replacementColor = Color.HSVToColor(originalAlpha, hsv);
            LogHelper.printDebug(() -> String.format("Original color: #%08X  replacement color: #%08X",
                            originalColor, replacementColor));
            return replacementColor;
        } catch (Exception exception) {
            resetSeekbarColor();
            return getSeekbarColorValue(originalColor);
        }
    }
}
