package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

public class HideAlbumCardsPatch {
    public static void hideAlbumCard(View view) {
        if (!Settings.HIDE_ALBUM_CARDS.getBoolean()) return;
        Utils.hideViewByLayoutParams(view);
    }
}