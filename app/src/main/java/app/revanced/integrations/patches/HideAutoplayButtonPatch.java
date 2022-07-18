package app.revanced.integrations.patches;

import android.view.View;
import android.view.ViewStub;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideAutoplayButtonPatch {

    public static void hideAutoplayButton(ViewStub stub) {
        if (!SettingsEnum.AUTOPLAY_BUTTON_SHOWN.getBoolean()) {
            LogHelper.debug(HideAutoplayButtonPatch.class, "Hiding the ViewStub:" + stub.toString());
            stub.setVisibility(View.GONE);
        }
    }

    public static boolean autoPlayEnabled() {
        return !SettingsEnum.AUTOPLAY_BUTTON_SHOWN.getBoolean();
    }

}
