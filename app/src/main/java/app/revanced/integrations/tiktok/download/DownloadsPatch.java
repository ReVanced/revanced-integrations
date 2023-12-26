package app.revanced.integrations.tiktok.download;

import app.revanced.integrations.tiktok.settings.Settings;

public class DownloadsPatch {
    public static String getDownloadPath() {
        return Settings.DOWNLOAD_PATH.getString();
    }

    public static boolean shouldRemoveWatermark() {
        return Settings.DOWNLOAD_WATERMARK.getBoolean();
    }
}
