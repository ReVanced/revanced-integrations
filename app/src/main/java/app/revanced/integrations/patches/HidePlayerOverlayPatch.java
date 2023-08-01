package app.revanced.integrations.patches;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class HidePlayerOverlayPatch {
    public static void hidePlayerOverlay(ImageView view) {
        if (SettingsEnum.HIDE_PLAYER_OVERLAY.getBoolean()) {
            view.setImageResource(android.R.color.transparent);
            return;
        }

        int opacity = 255;
        try {
            opacity = Integer.parseInt(SettingsEnum.CHANGE_PLAYER_OVERLAY_OPACITY.getString());
        } catch (Exception e) {
            ReVancedUtils.showToastLong("Enter correct integer value of opacity!");
            return;
        }

        if (opacity < 0 || opacity > 255) {
            ReVancedUtils.showToastLong("Opacity value must be from 0 to 255!");
            return;
        }

        view.setImageAlpha(opacity);
    }
}
