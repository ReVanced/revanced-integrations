package app.revanced.integrations.videoplayer;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;

public class CopyVideoUrlButton {
    static BottomControlButton _button;

    public static void initializeButton(Object obj) {
        _button = new BottomControlButton(
                obj,
                "copy_video_url_button",
                SettingsEnum.COPY_VIDEO_URL_BUTTON_SHOWN.getBoolean(),
                view -> CopyVideoUrlPatch.copyUrl(false)
        );
    }

    public static void changeVisibility(boolean z) {
        if (_button != null) _button.changeVisibility(z);
    }
}