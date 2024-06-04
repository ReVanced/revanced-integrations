package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;

import android.widget.ImageView;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class TabletLayoutPatch {

    private static final boolean TABLET_LAYOUT_ENABLED = Settings.TABLET_LAYOUT.get();

    private static final boolean TABLET_MINI_PLAYER_ENABLED = Settings.TABLET_MINI_PLAYER.get();

    private static final boolean TABLET_MINI_PLAYER_MODERN_ENABLED
            = TABLET_MINI_PLAYER_ENABLED && Settings.TABLET_MINI_PLAYER_MODERN.get();

    private static final boolean TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE_ENABLED
            = TABLET_MINI_PLAYER_MODERN_ENABLED && Settings.TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE.get();

    private static final boolean TABLET_MINI_PLAYER_MODERN_HIDE_REWIND_FORWARD_ENABLED
            = TABLET_MINI_PLAYER_MODERN_ENABLED && Settings.TABLET_MINI_PLAYER_MODERN_HIDE_REWIND_FORWARD.get();

    private static final int TABLET_MINI_PLAYER_MODERN_OPACITY_LEVEL;

    static {
        int opacity = Settings.TABLET_MINI_PLAYER_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_tablet_mini_player_opacity_invalid_toast"));
            Settings.TABLET_MINI_PLAYER_OPACITY.resetToDefault();
            opacity = Settings.TABLET_MINI_PLAYER_OPACITY.defaultValue;
        }

        TABLET_MINI_PLAYER_MODERN_OPACITY_LEVEL = (opacity * 255) / 100;
    }

    /**
     * Injection point.
     */
    public static boolean getTabletLayoutEnabled() {
        return TABLET_LAYOUT_ENABLED;
    }

    /**
     * Injection point.
     */
    public static boolean getTabletMiniPlayerOverride(boolean original) {
        return TABLET_MINI_PLAYER_ENABLED || original;
    }

    /**
     * Injection point.
     */
    public static boolean getModernTabletMiniPlayerOverrideBoolean(boolean original) {
        return TABLET_MINI_PLAYER_MODERN_ENABLED || original;
    }

    /**
     * Injection point.
     */
    public static int getModernTabletMiniPlayerOverrideInt(int original) {
        return TABLET_MINI_PLAYER_MODERN_ENABLED
                ? 1
                : original;
    }

    /**
     * Injection point.
     */
    public static void adjustModernTabletMiniPlayerOpacity(ImageView view) {
        if (TABLET_MINI_PLAYER_MODERN_ENABLED) {
            view.setImageAlpha(TABLET_MINI_PLAYER_MODERN_OPACITY_LEVEL);
        }
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerExpandClose(ImageView view) {
        Utils.removeViewFromParentUnderCondition(TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE_ENABLED, view);
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerRewindForward(ImageView view) {
        Utils.removeViewFromParentUnderCondition(TABLET_MINI_PLAYER_MODERN_HIDE_REWIND_FORWARD_ENABLED, view);
    }
}
