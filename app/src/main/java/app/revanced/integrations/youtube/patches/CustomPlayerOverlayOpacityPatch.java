package app.revanced.integrations.youtube.patches;

import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

public class CustomPlayerOverlayOpacityPatch {
    private static final int DEFAULT_OPACITY = (int) Settings.PLAYER_OVERLAY_OPACITY.defaultValue;

    public static void changeOpacity(ImageView imageView) {
        int opacity = Settings.PLAYER_OVERLAY_OPACITY.getInt();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong("Player overlay opacity must be between 0-100");
            Settings.PLAYER_OVERLAY_OPACITY.saveValue(DEFAULT_OPACITY);
            opacity = DEFAULT_OPACITY;
        }

        imageView.setImageAlpha((opacity * 255) / 100);
    }
}
