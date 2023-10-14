package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.StringRef.str;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.Utils;

public class CustomPlayerOverlayOpacityPatch {
    private static final int DEFAULT_OPACITY = (int) SettingsEnum.PLAYER_OVERLAY_OPACITY.defaultValue;

    public static void changeOpacity(ImageView imageView) {
        int opacity = SettingsEnum.PLAYER_OVERLAY_OPACITY.getInt();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_player_overlay_opacity_parse_error"));
            SettingsEnum.PLAYER_OVERLAY_OPACITY.saveValue(DEFAULT_OPACITY);
            opacity = DEFAULT_OPACITY;
        }

        imageView.setImageAlpha((opacity * 255) / 100);
    }
}
