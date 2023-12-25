package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Settings;

public class HideInfoCardsPatch {
    public static void hideInfoCardsIncognito(View view) {
        if (!Settings.HIDE_INFO_CARDS.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

    public static boolean hideInfoCardsMethodCall() {
        return Settings.HIDE_INFO_CARDS.getBoolean();
    }
}
