package app.revanced.integrations.tiktok.settingsmenu.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import app.revanced.integrations.shared.settings.Setting;

public class InputTextPreference extends EditTextPreference {

    public InputTextPreference(Context context, String title, String summary, Setting setting) {
        super(context);
        this.setTitle(title);
        this.setSummary(summary);
        this.setKey(setting.key);
        this.setText(setting.getString());
    }
}
