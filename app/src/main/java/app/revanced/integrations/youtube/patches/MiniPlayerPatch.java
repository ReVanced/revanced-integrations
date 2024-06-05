package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.MiniPlayerPatch.MiniPlayerType.TABLET_MODERN;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class MiniPlayerPatch {

    /**
     * Mini player type. Null fields indicates to use the original un-patched value.
     */
    public enum MiniPlayerType {
        /** Unmodified type, and same as unpatched */
        ORIGINAL(null, null),
        PHONE(false, null),
        PHONE_MODERN(false, 3),
        TABLET(true, null),
        TABLET_MODERN(true, 1),
        /**
         * Modern design with layout of old tablet mini player.
         * Has some bugs with vertical videos and empty sub texts,
         * and not substantially different from the old tablet mini player so it's not currently exposed.
         *
         * If anyone wants to try it anyways, then manually edit the imported data and
         * change the type to this enum name.
         */
        TABLET_MODERN_2(true, 2);

        @Nullable
        final Boolean isTablet;
        @Nullable
        final Integer modernIntValue;

        MiniPlayerType(@Nullable Boolean isTablet, @Nullable Integer modernIntValue) {
            this.isTablet = isTablet;
            this.modernIntValue = modernIntValue;
        }

        public boolean isModern() {
            return modernIntValue != null;
        }
    }

    public static final class MiniPlayerModernAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.TABLET_MINI_PLAYER_TYPE.get().modernIntValue != null;
        }
    }

    public static final class MiniPlayerTabletModernAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.TABLET_MINI_PLAYER_TYPE.get() == TABLET_MODERN;
        }
    }

    private static final MiniPlayerType CURRENT_TYPE = Settings.TABLET_MINI_PLAYER_TYPE.get();
    private static final boolean TABLET_MODERN_SELECTED = (CURRENT_TYPE == TABLET_MODERN);

    private static final boolean HIDE_EXPAND_CLOSE_BUTTONS_ENABLED =
            CURRENT_TYPE.isModern() && Settings.TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE.get();

    private static final boolean HIDE_SUBTEXTS_ENABLED =
            CURRENT_TYPE.isModern() && Settings.TABLET_MINI_PLAYER_MODERN_HIDE_SUB_TEXT.get();

    private static final boolean HIDE_REWIND_FORWARD_ENABLED =
            TABLET_MODERN_SELECTED && Settings.TABLET_MINI_PLAYER_MODERN_HIDE_REWIND_FORWARD.get();

    private static final int TABLET_MODERN_OPACITY_LEVEL;

    static {
        int opacity = Settings.TABLET_MINI_PLAYER_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_mini_player_tablet_modern_opacity_invalid_toast"));
            Settings.TABLET_MINI_PLAYER_OPACITY.resetToDefault();
            opacity = Settings.TABLET_MINI_PLAYER_OPACITY.defaultValue;
        }

        TABLET_MODERN_OPACITY_LEVEL = (opacity * 255) / 100;
    }

    /**
     * Injection point.
     */
    public static boolean getTabletMiniPlayerOverride(boolean original) {
        Boolean isTablet = CURRENT_TYPE.isTablet;
        return isTablet == null
                ? original
                : isTablet;
    }

    /**
     * Injection point.
     */
    public static boolean getModernMiniPlayerOverrideBoolean(boolean original) {
        return CURRENT_TYPE.isModern() || original;
    }

    /**
     * Injection point.
     */
    public static int getModernMiniPlayerOverrideInt(int original) {
        Integer modernValue = CURRENT_TYPE.modernIntValue;
        return modernValue == null
                ? original
                : modernValue;
    }

    /**
     * Injection point.
     */
    public static void adjustModernMiniPlayerOpacity(ImageView view) {
        if (TABLET_MODERN_SELECTED) {
            view.setImageAlpha(TABLET_MODERN_OPACITY_LEVEL);
        }
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerExpandClose(ImageView view) {
        Utils.removeViewFromParentUnderCondition(HIDE_EXPAND_CLOSE_BUTTONS_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerRewindForward(ImageView view) {
        Utils.removeViewFromParentUnderCondition(HIDE_REWIND_FORWARD_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerView(View view) {
        // Different subviews are passed in, but only TextView and layouts are of interest here.
        final boolean hideView = HIDE_SUBTEXTS_ENABLED && (view instanceof TextView || view instanceof LinearLayout);
        Utils.removeViewFromParentUnderCondition(hideView, view);
    }
}
