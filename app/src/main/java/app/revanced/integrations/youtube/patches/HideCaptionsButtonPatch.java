package app.revanced.integrations.youtube.patches;

import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.SettingsEnum;
import app.revanced.integrations.youtube.utils.LogHelper;

public class HideCaptionsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.hidecaptionsbutton.patch.HideCaptionsButtonPatch
    public static void hideCaptionsButton(ImageView imageView) {
        imageView.setVisibility(SettingsEnum.HIDE_CAPTIONS_BUTTON.getBoolean() ? ImageView.GONE : ImageView.VISIBLE);
    }
}
