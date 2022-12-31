package app.revanced.integrations.videoplayer;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;

public class CopyVideoUrlTimestampButton {
    static BottomControlButton _button;

    public static void initializeButton(Object obj) {
        _button = new BottomControlButton(
                obj,
                "copy_video_url_timestamp_button",
                SettingsEnum.COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN.getBoolean(),
                view -> CopyVideoUrlPatch.copyUrl(true)
        );
    }

    public static void changeVisibility(boolean z) {
        if (_button != null) _button.changeVisibility(z);
    }
}