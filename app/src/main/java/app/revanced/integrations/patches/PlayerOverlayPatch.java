package app.revanced.integrations.patches;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class PlayerOverlayPatch {
    private static final boolean HIDE_PLAYER_OVERLAY = SettingsEnum.HIDE_PLAYER_OVERLAY.getBoolean();
    private static final int DEFAULT_OPACITY = (int) SettingsEnum.CHANGE_PLAYER_OVERLAY_OPACITY_VALUE.defaultValue;
    private static int opacity = DEFAULT_OPACITY;

    static {
        if (!HIDE_PLAYER_OVERLAY)
            loadCustomOpacityValue();
    }

    public static void hidePlayerOverlay(ImageView imageView) {
        if (!HIDE_PLAYER_OVERLAY) return;

        imageView.setImageResource(android.R.color.transparent);
    }

    public static void changePlayerOverlay(ImageView imageView) {
        if (HIDE_PLAYER_OVERLAY) return;

        imageView.setImageAlpha(mapOpacity(opacity));
    }

    private static void loadCustomOpacityValue() {
        try {
            opacity = SettingsEnum.CHANGE_PLAYER_OVERLAY_OPACITY_VALUE.getInt();
            if (opacity < 0 || opacity > 100)
                throw new IllegalArgumentException();
        } catch (Exception e) {
            ReVancedUtils.showToastLong("Player overlay opacity must be 0-100!");
            SettingsEnum.CHANGE_PLAYER_OVERLAY_OPACITY_VALUE.saveValue(DEFAULT_OPACITY);
            loadCustomOpacityValue();
        }
    }

    private static int mapOpacity(int opacity) {
        return (opacity * 255) / 100;
    }
}
