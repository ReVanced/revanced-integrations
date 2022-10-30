package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideLiveChatFullScreenButtonPatch {
    //Used by app.revanced.patches.youtube.layout.comments.patch.CommentsPatch
    public static int fullScreenLiveChatButtonId = 0;

    public static int hideLiveChatFullScreenButton(View view, int originalValue) {
        if (!SettingsEnum.HIDE_LIVE_CHAT_FULL_SCREEN_BUTTON.getBoolean() || view.getId() != fullScreenLiveChatButtonId) return originalValue;

        return 8;
    }
}
