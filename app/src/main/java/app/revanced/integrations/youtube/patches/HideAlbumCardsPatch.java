package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public class HideAlbumCardsPatch {
    public static void hideAlbumCard(View view) {
        if (!Settings.HIDE_ALBUM_CARDS.getBoolean()) return;
        ReVancedUtils.hideViewByLayoutParams(view);
    }
}