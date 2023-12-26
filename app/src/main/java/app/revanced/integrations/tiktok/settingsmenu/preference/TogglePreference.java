package app.revanced.integrations.tiktok.settingsmenu.preference;

import android.content.Context;
import android.preference.SwitchPreference;
import app.revanced.integrations.shared.settings.Setting;

@SuppressWarnings("deprecation")
public class TogglePreference extends SwitchPreference {
    public TogglePreference(Context context, String title, String summary, Setting setting) {
        super(context);
        this.setTitle(title);
        this.setSummary(summary);
        this.setKey(setting.key);
        this.setChecked(setting.getBoolean());
    }
}
