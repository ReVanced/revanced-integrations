package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardsPatch {
    public static boolean hideDrawerHeader() {
        return SettingsEnum.INFO_CARDS_SHOWN.getBoolean();
    }

    public static void hide(View view) {
        if (SettingsEnum.INFO_CARDS_SHOWN.getBoolean()) return;
        view.setVisibility(View.GONE);
    }
}
