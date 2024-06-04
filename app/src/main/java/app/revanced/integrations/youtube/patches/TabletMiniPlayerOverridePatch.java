package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class TabletMiniPlayerOverridePatch {

    private static final Boolean TABLET_MINI_PLAYER_ENABLED = Settings.TABLET_MINI_PLAYER.get();
    private static final Boolean TABLET_MINI_PLAYER_MODERN_ENABLED = Settings.TABLET_MINI_PLAYER_MODERN.get();
    private static final Boolean TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE_ENABLED = Settings.TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE.get();

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
        if (TABLET_MINI_PLAYER_MODERN_ENABLED) {
            return 1;
        }

        return original;
    }

    /**
     * Injection point.
     */
    public static void hideModernMiniPlayerButtonView(View view) {
        if (TABLET_MINI_PLAYER_MODERN_ENABLED) {
            Utils.removeViewFromParentUnderCondition(TABLET_MINI_PLAYER_MODERN_HIDE_EXPAND_CLOSE_ENABLED, view);
        }
    }

}
