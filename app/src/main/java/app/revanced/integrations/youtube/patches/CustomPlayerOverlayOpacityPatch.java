package app.revanced.integrations.youtube.patches;

import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.Setting;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public class CustomPlayerOverlayOpacityPatch {
    private static final int DEFAULT_OPACITY = (int) Setting.PLAYER_OVERLAY_OPACITY.defaultValue;

    public static void changeOpacity(ImageView imageView) {
        int opacity = Setting.PLAYER_OVERLAY_OPACITY.getInt();

        if (opacity < 0 || opacity > 100) {
            ReVancedUtils.showToastLong("Player overlay opacity must be between 0-100");
            Setting.PLAYER_OVERLAY_OPACITY.saveValue(DEFAULT_OPACITY);
            opacity = DEFAULT_OPACITY;
        }

        imageView.setImageAlpha((opacity * 255) / 100);
    }
}
