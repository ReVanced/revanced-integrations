package app.revanced.integrations.patches.theme;

import android.graphics.Color;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class ThemePatch {

    /**
     * Default color of seekbar.
     */
    private static final int ORIGINAL_SEEKBAR_COLOR = 0xFFFF0000;

    /**
     * Default YouTube seekbar color brightness.
     */
    private static final float ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS;

    /**
     * Color value of {@link SettingsEnum#SEEKBAR_COLOR}
     */
    private static int customSeekbarColor;

    /**
     * Custom seekbar hue, saturation, and brightness values.
     */
    private static final float[] customSeekbarColorHSV = new float[3];

    static {
        float[] hsv = new float[3];
        Color.colorToHSV(ORIGINAL_SEEKBAR_COLOR, hsv);
        ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS = hsv[2];

        loadCustomSeekbarColorHSV();
    }

    private static void loadCustomSeekbarColorHSV() {
        try {
            customSeekbarColor = Color.parseColor(SettingsEnum.SEEKBAR_COLOR.getString());
            Color.colorToHSV(customSeekbarColor, customSeekbarColorHSV);
        } catch (Exception ex) {
            ReVancedUtils.showToastShort("Invalid seekbar color value. Using default value.");
            SettingsEnum.SEEKBAR_COLOR.saveValue(SettingsEnum.SEEKBAR_COLOR.defaultValue);
            loadCustomSeekbarColorHSV();
        }
    }

    public static int getCustomSeekbarColor() {
        return customSeekbarColor;
    }

    /**
     * Injection point.
     *
     * Overrides color when seekbar is clicked, and all Litho components that use the YouTube seekbar color.
     */
    public static int getSeekbarColorOverride(int colorValue) {
        return colorValue == ORIGINAL_SEEKBAR_COLOR
                ? getSeekbarColorValue(ORIGINAL_SEEKBAR_COLOR)
                : colorValue;
    }

    /**
     * Injection point.
     *
     * Unconditionally changes the color to the seekbar custom color, while retaining
     * the brightness and alpha changes of the parameter value compared to the original seekbar color.
     */
    public static int getSeekbarColorValue(int originalColor) {
        try {
            if (SettingsEnum.HIDE_SEEKBAR.getBoolean()) {
                return 0x00000000;
            }
            if (customSeekbarColor == ORIGINAL_SEEKBAR_COLOR) {
                return originalColor; // Nothing to do
            }
            final int alphaDifference = Color.alpha(originalColor) - Color.alpha(ORIGINAL_SEEKBAR_COLOR);

            // The seekbar uses the same color but different brightness for different situations.
            float[] hsv = new float[3];
            Color.colorToHSV(originalColor, hsv);
            final float brightnessDifference = hsv[2] - ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS;

            // Apply the brightness difference to the custom seekbar color.
            hsv[0] = customSeekbarColorHSV[0];
            hsv[1] = customSeekbarColorHSV[1];
            hsv[2] = Math.max(0, customSeekbarColorHSV[2] + brightnessDifference);

            final int replacementAlpha = Math.max(0, Color.alpha(customSeekbarColor) + alphaDifference);
            final int replacementColor = Color.HSVToColor(replacementAlpha, hsv);
            LogHelper.printDebug(() -> String.format("Original color: #%08X  replacement color: #%08X",
                            originalColor, replacementColor));
            return replacementColor;
        } catch (Exception ex) {
            LogHelper.printException(() -> "getSeekbarColorValue failure", ex);
            return originalColor;
        }
    }
}
