package app.revanced.integrations.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.integrations.tiktok.settings.Settings;
import app.revanced.integrations.tiktok.settingsmenu.SettingsStatus;
import app.revanced.integrations.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.integrations.tiktok.settingsmenu.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class DownloadsPreferenceCategory extends ConditionalPreferenceCategory {
    public DownloadsPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Downloads");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.downloadEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new DownloadPathPreference(
                context,
                "Download path",
                Settings.DOWNLOAD_PATH
        ));
        addPreference(new TogglePreference(
                context,
                "Remove watermark", "",
                Settings.DOWNLOAD_WATERMARK
        ));
    }
}
