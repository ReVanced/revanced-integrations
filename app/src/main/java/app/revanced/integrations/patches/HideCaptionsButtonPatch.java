package app.revanced.integrations.patches;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideCaptionsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.hidecaptionsbutton.patch.HideCaptionsButtonPatch
    public static void hideCaptionsButton(ImageView imageView) {
        boolean enabled = SettingsEnum.HIDE_CAPTIONS_BUTTON.getBoolean();
        String message =  "Create button: " + (enabled ? "shown" : "hidden");
        LogHelper.debug(HideCreateButtonPatch.class, message);
        imageView.setVisibility(enabled ? ImageView.GONE : ImageView.VISIBLE);
    }
}
