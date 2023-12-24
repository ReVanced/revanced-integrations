package app.revanced.integrations.tiktok.download;

import app.revanced.integrations.tiktok.settings.SettingsEnum;

public class DownloadsPatch {
    public static String getDownloadPath() {
        return SettingsEnum.DOWNLOAD_PATH.getString();
    }

    public static boolean shouldRemoveWatermark() {
        return SettingsEnum.DOWNLOAD_WATERMARK.getBoolean();
    }
}
