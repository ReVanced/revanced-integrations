package app.revanced.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settingsmenu.SettingsStatus;
import app.revanced.tiktok.settingsmenu.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class SimSpoofPreferenceCategory extends ConditionalPreferenceCategory {
    public SimSpoofPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);

        setTitle("Integrations");

        addPreference(new TogglePreference(context,
                "Enable debug log",
                "Show integration debug log.",
                SettingsEnum.DEBUG
        ));
    }


    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.simSpoofEnabled;
    }
}
