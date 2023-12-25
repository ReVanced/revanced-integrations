package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Setting;

public class HideInfoCardsPatch {
    public static void hideInfoCardsIncognito(View view) {
        if (!Setting.HIDE_INFO_CARDS.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

    public static boolean hideInfoCardsMethodCall() {
        return Setting.HIDE_INFO_CARDS.getBoolean();
    }
}
