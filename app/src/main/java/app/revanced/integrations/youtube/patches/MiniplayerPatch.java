package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.MiniplayerPatch.MiniplayerType.ORIGINAL;
import static app.revanced.integrations.youtube.patches.MiniplayerPatch.MiniplayerType.TABLET_MODERN;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class MiniplayerPatch {

    /**
     * Mini player type. Null fields indicates to use the original un-patched value.
     */
    public enum MiniplayerType {
        /** Unmodified type, and same as un-patched. */
        ORIGINAL(null, null),
        PHONE(false, null),
        PHONE_MODERN(null, 3),
        TABLET(true, null),
        TABLET_MODERN(null, 1),
        /**
         * Modern design with layout of old tablet miniplayer.
         * Has some bugs with vertical videos and empty sub texts,
         * and not substantially different from the old tablet mini player so it's currently not selectable.
         *
         * If anyone wants to try it anyways, then manually edit the imported data and
         * change the type to this enum name.
         */
        TABLET_MODERN_2(false, 2);

        /**
         * Legacy tablet hook value.
         */
        @Nullable
        final Boolean isTablet;

        @Nullable
        final Integer modernIntValue;

        MiniplayerType(@Nullable Boolean isTablet, @Nullable Integer modernIntValue) {
            this.isTablet = isTablet;
            this.modernIntValue = modernIntValue;
        }

        public boolean isModern() {
            return modernIntValue != null;
        }
    }

    public static final class MiniplayerModernAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.TABLET_MINIPLAYER_TYPE.get().isModern();
        }
    }

    public static final class MiniplayerTabletModernAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.TABLET_MINIPLAYER_TYPE.get() == TABLET_MODERN;
        }
    }

    private static final MiniplayerType CURRENT_TYPE = Settings.TABLET_MINIPLAYER_TYPE.get();
    private static final boolean TABLET_MODERN_SELECTED = (CURRENT_TYPE == TABLET_MODERN);

    private static final boolean HIDE_EXPAND_CLOSE_BUTTONS_ENABLED =
            CURRENT_TYPE.isModern() && Settings.TABLET_MINIPLAYER_HIDE_EXPAND_CLOSE.get();

    private static final boolean HIDE_SUBTEXTS_ENABLED =
            CURRENT_TYPE.isModern() && Settings.TABLET_MINIPLAYER_HIDE_SUB_TEXT.get();

    private static final boolean HIDE_REWIND_FORWARD_ENABLED =
            TABLET_MODERN_SELECTED && Settings.TABLET_MINIPLAYER_HIDE_REWIND_FORWARD.get();

    private static final int OPACITY_LEVEL;

    static {
        int opacity = Settings.TABLET_MINIPLAYER_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_miniplayer_opacity_invalid_toast"));
            Settings.TABLET_MINIPLAYER_OPACITY.resetToDefault();
            opacity = Settings.TABLET_MINIPLAYER_OPACITY.defaultValue;
        }

        OPACITY_LEVEL = (opacity * 255) / 100;
    }

    /**
     * Injection point.
     */
    public static boolean getTabletOverride(boolean original) {
        Boolean isTablet = CURRENT_TYPE.isTablet;
        return isTablet == null
                ? original
                : isTablet;
    }

    /**
     * Injection point.
     */
    public static boolean getModernOverride(boolean original) {
        if (CURRENT_TYPE == ORIGINAL) {
            return original;
        }

        return CURRENT_TYPE.modernIntValue != null;
    }

    /**
     * Injection point.
     */
    public static int getModernOverrideType(int original) {
        Integer modernValue = CURRENT_TYPE.modernIntValue;
        return modernValue == null
                ? original
                : modernValue;
    }

    /**
     * Injection point.
     */
    public static void adjustMiniplayerOpacity(ImageView view) {
        if (TABLET_MODERN_SELECTED) {
            view.setImageAlpha(OPACITY_LEVEL);
        }
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerExpandClose(ImageView view) {
        Utils.removeViewFromParentUnderCondition(HIDE_EXPAND_CLOSE_BUTTONS_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerRewindForward(ImageView view) {
        Utils.removeViewFromParentUnderCondition(HIDE_REWIND_FORWARD_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideMiniplayerSubTexts(View view) {
        // Different subviews are passed in, but only TextView and layouts are of interest here.
        final boolean hideView = HIDE_SUBTEXTS_ENABLED && (view instanceof TextView || view instanceof LinearLayout);
        Utils.removeViewFromParentUnderCondition(hideView, view);
    }
}
