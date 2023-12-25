package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Setting;

public class FullscreenPanelsRemoverPatch {
    public static int getFullscreenPanelsVisibility() {
        return Setting.HIDE_FULLSCREEN_PANELS.getBoolean() ? View.GONE : View.VISIBLE;
    }
}
