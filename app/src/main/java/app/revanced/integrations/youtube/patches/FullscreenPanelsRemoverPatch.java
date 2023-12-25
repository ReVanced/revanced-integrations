package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Settings;

public class FullscreenPanelsRemoverPatch {
    public static int getFullscreenPanelsVisibility() {
        return Settings.HIDE_FULLSCREEN_PANELS.getBoolean() ? View.GONE : View.VISIBLE;
    }
}
