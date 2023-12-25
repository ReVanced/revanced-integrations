package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.CopyVideoUrlPatch;
import app.revanced.integrations.youtube.settings.Setting;
import app.revanced.integrations.youtube.utils.LogHelper;

public class CopyVideoUrlButton extends BottomControlButton {
    @Nullable
    private static CopyVideoUrlButton instance;

    public CopyVideoUrlButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "copy_video_url_button",
                Setting.COPY_VIDEO_URL,
                view -> CopyVideoUrlPatch.copyUrl(false),
                view -> {
                    CopyVideoUrlPatch.copyUrl(true);
                    return true;
                }
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new CopyVideoUrlButton((ViewGroup) view);
        } catch (Exception ex) {
            LogHelper.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }
}