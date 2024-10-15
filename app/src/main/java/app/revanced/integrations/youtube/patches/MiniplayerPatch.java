package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.MiniplayerPatch.MiniplayerType.*;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public final class MiniplayerPatch {

    /**
     * Mini player type. Null fields indicates to use the original un-patched value.
     */
    public enum MiniplayerType {
        /** Unmodified type, and same as un-patched. */
        ORIGINAL(null, null),
        PHONE(false, null),
        TABLET(true, null),
        MODERN_1(null, 1),
        MODERN_2(null, 2),
        MODERN_3(null, 3),
        /**
         * Half broken miniplayer, that might be work in progress or left over abandoned code.
         * Can force this type by editing the import/export settings.
         */
        MODERN_4(null, 4);

        /**
         * Legacy tablet hook value.
         */
        @Nullable
        final Boolean legacyTabletOverride;

        /**
         * Modern player type used by YT.
         */
        @Nullable
        final Integer modernPlayerType;

        MiniplayerType(@Nullable Boolean legacyTabletOverride, @Nullable Integer modernPlayerType) {
            this.legacyTabletOverride = legacyTabletOverride;
            this.modernPlayerType = modernPlayerType;
        }

        public boolean isModern() {
            return modernPlayerType != null;
        }
    }

    private static final int MINIPLAYER_SIZE;

    static {
        // YT appears to use the device screen dip width, plus an unknown fixed horizontal padding size.
        DisplayMetrics displayMetrics = Utils.getContext().getResources().getDisplayMetrics();
        final int deviceDipWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);

        // YT seems to use a minimum height to calculate the minimum miniplayer width based on the video.
        // 170 seems to be the smallest that can be used and using less makes no difference.
        final int WIDTH_DIP_MIN = 170; // Seems to be the smallest that works.
        final int HORIZONTAL_PADDING_DIP = 15; // Estimated padding.
        // Round down to the nearest 5 pixels, to keep any error toasts easier to read.
        final int WIDTH_DIP_MAX = 5 * ((deviceDipWidth - HORIZONTAL_PADDING_DIP) / 5);
        Logger.printDebug(() -> "Screen dip width: " + deviceDipWidth + " maxWidth: " + WIDTH_DIP_MAX);

        int dipWidth = Settings.MINIPLAYER_WIDTH_DIP.get();

        if (dipWidth < WIDTH_DIP_MIN || dipWidth > WIDTH_DIP_MAX) {
            Utils.showToastLong(str("revanced_miniplayer_width_dip_invalid_toast",
                    WIDTH_DIP_MIN, WIDTH_DIP_MAX));

            // Instead of resetting, clamp the size at the bounds.
            dipWidth = Math.max(WIDTH_DIP_MIN, Math.min(dipWidth, WIDTH_DIP_MAX));
            Settings.MINIPLAYER_WIDTH_DIP.save(dipWidth);
        }

        MINIPLAYER_SIZE = dipWidth;
    }

    private static final boolean IS_19_20_OR_GREATER = Utils.getAppVersionName().compareTo("19.20.00") >= 0;
    private static final boolean IS_19_21_OR_GREATER = Utils.getAppVersionName().compareTo("19.21.00") >= 0;
    private static final boolean IS_19_26_OR_GREATER = Utils.getAppVersionName().compareTo("19.26.00") >= 0;
    private static final boolean IS_19_29_OR_GREATER = Utils.getAppVersionName().compareTo("19.29.00") >= 0;

    /**
     * Modern subtitle overlay for {@link MiniplayerType#MODERN_2}.
     * Resource is not present in older targets, and this field will be zero.
     */
    private static final int MODERN_OVERLAY_SUBTITLE_TEXT
            = Utils.getResourceIdentifier("modern_miniplayer_subtitle_text", "id");

    private static final MiniplayerType CURRENT_TYPE = Settings.MINIPLAYER_TYPE.get();

    /**
     * Cannot turn off double tap with modern 2 or 3 with later targets,
     * as forcing it off breakings tapping the miniplayer.
     */
    private static final boolean DOUBLE_TAP_ACTION_ENABLED =
            // 19.29+ is very broken if double tap is not enabled.
            IS_19_29_OR_GREATER ||
                    (CURRENT_TYPE.isModern() && Settings.MINIPLAYER_DOUBLE_TAP_ACTION.get());

    private static final boolean DRAG_AND_DROP_ENABLED =
            CURRENT_TYPE.isModern() && Settings.MINIPLAYER_DRAG_AND_DROP.get();

    private static final boolean HIDE_EXPAND_CLOSE_ENABLED =
            Settings.MINIPLAYER_HIDE_EXPAND_CLOSE.get()
                    && Settings.MINIPLAYER_HIDE_EXPAND_CLOSE.isAvailable();

    private static final boolean HIDE_SUBTEXT_ENABLED =
            (CURRENT_TYPE == MODERN_1 || CURRENT_TYPE == MODERN_3) && Settings.MINIPLAYER_HIDE_SUBTEXT.get();

    private static final boolean HIDE_REWIND_FORWARD_ENABLED =
            CURRENT_TYPE == MODERN_1 && Settings.MINIPLAYER_HIDE_REWIND_FORWARD.get();

    private static final boolean MINIPLAYER_ROUNDED_CORNERS_ENABLED =
            Settings.MINIPLAYER_ROUNDED_CORNERS.get();

    /**
     * Remove a broken and always present subtitle text that is only
     * present with {@link MiniplayerType#MODERN_2}. Bug was fixed in 19.21.
     */
    private static final boolean HIDE_BROKEN_MODERN_2_SUBTITLE =
            CURRENT_TYPE == MODERN_2 && !IS_19_21_OR_GREATER;

    private static final int OPACITY_LEVEL;

    public static final class MiniplayerHideExpandCloseAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            MiniplayerType type = Settings.MINIPLAYER_TYPE.get();
            return (!IS_19_20_OR_GREATER && (type == MODERN_1 || type == MODERN_3))
                    || (!IS_19_26_OR_GREATER && type == MODERN_1
                        && !Settings.MINIPLAYER_DOUBLE_TAP_ACTION.get() && !Settings.MINIPLAYER_DRAG_AND_DROP.get())
                    || (IS_19_29_OR_GREATER && type == MODERN_3);
        }
    }

    static {
        int opacity = Settings.MINIPLAYER_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_miniplayer_opacity_invalid_toast"));
            Settings.MINIPLAYER_OPACITY.resetToDefault();
            opacity = Settings.MINIPLAYER_OPACITY.defaultValue;
        }

        OPACITY_LEVEL = (opacity * 255) / 100;
    }

    /**
     * Injection point.
     */
    public static boolean getLegacyTabletMiniplayerOverride(boolean original) {
        Boolean isTablet = CURRENT_TYPE.legacyTabletOverride;
        return isTablet == null
                ? original
                : isTablet;
    }

    /**
     * Injection point.
     */
    public static boolean getModernMiniplayerOverride(boolean original) {
        return CURRENT_TYPE == ORIGINAL
                ? original
                : CURRENT_TYPE.isModern();
    }

    /**
     * Injection point.
     */
    public static int getModernMiniplayerOverrideType(int original) {
        Integer modernValue = CURRENT_TYPE.modernPlayerType;
        return modernValue == null
                ? original
                : modernValue;
    }

    /**
     * Injection point.
     */
    public static void adjustMiniplayerOpacity(ImageView view) {
        if (CURRENT_TYPE == MODERN_1) {
            view.setImageAlpha(OPACITY_LEVEL);
        }
    }

    /**
     * Injection point.
     */
    public static boolean getModernFeatureFlagsActiveOverride(boolean original) {
        if (original) Logger.printDebug(() -> "getModernFeatureFlagsActiveOverride original: " + original);

        if (CURRENT_TYPE == ORIGINAL) {
            return original;
        }

        return CURRENT_TYPE.isModern();
    }

    /**
     * Injection point.
     */
    public static boolean enableMiniplayerDoubleTapAction(boolean original) {
        if (original) Logger.printDebug(() -> "enableMiniplayerDoubleTapAction original: " + true);

        if (CURRENT_TYPE == ORIGINAL) {
            return original;
        }

        return DOUBLE_TAP_ACTION_ENABLED;
    }

    /**
     * Injection point.
     */
    public static boolean enableMiniplayerDragAndDrop(boolean original) {
        if (original) Logger.printDebug(() -> "enableMiniplayerDragAndDrop original: " + true);

        if (CURRENT_TYPE == ORIGINAL) {
            return original;
        }

        return DRAG_AND_DROP_ENABLED;
    }


    /**
     * Injection point.
     */
    public static boolean setRoundedCorners(boolean original) {
        if (original) Logger.printDebug(() -> "setRoundedCorners original: " + true);

        if (CURRENT_TYPE.isModern()) {
            return MINIPLAYER_ROUNDED_CORNERS_ENABLED;
        }

        return original;
    }

    /**
     * Injection point.
     */
    public static int setMiniplayerDefaultSize(int original) {
        if (CURRENT_TYPE.isModern()) {
            return MINIPLAYER_SIZE;
        }

        return original;
    }

    /**
     * Injection point.
     */
    public static float setMovementBoundFactor(float original) {
        // Not clear if customizing this is useful or not.
        // So for now just log this and use the original value.
        if (original != 1.0) Logger.printDebug(() -> "setMovementBoundFactor original: " + original);

        return original;
    }

    /**
     * Injection point.
     */
    public static boolean setDropShadow(boolean original) {
        if (original) Logger.printDebug(() -> "setViewElevation original: " + true);

        return original;
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerExpandClose(ImageView view) {
        Utils.hideViewByRemovingFromParentUnderCondition(HIDE_EXPAND_CLOSE_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerRewindForward(ImageView view) {
        Utils.hideViewByRemovingFromParentUnderCondition(HIDE_REWIND_FORWARD_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerSubTexts(View view) {
        try {
            // Different subviews are passed in, but only TextView is of interest here.
            if (HIDE_SUBTEXT_ENABLED && view instanceof TextView) {
                Logger.printDebug(() -> "Hiding subtext view");
                Utils.hideViewByRemovingFromParentUnderCondition(true, view);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "hideMiniplayerSubTexts failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void playerOverlayGroupCreated(View group) {
        try {
            if (HIDE_BROKEN_MODERN_2_SUBTITLE && MODERN_OVERLAY_SUBTITLE_TEXT != 0) {
                if (group instanceof ViewGroup) {
                    View subtitleText = Utils.getChildView((ViewGroup) group, true,
                            view -> view.getId() == MODERN_OVERLAY_SUBTITLE_TEXT);

                    if (subtitleText != null) {
                        subtitleText.setVisibility(View.GONE);
                        Logger.printDebug(() -> "Modern overlay subtitle view set to hidden");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "playerOverlayGroupCreated failure", ex);
        }
    }
}
