package app.revanced.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

@SuppressWarnings("deprecation")
public abstract class ConditionalPreferenceCategory extends PreferenceCategory {
    public ConditionalPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context);

        if (getSettingsStatus()) screen.addPreference(this);
    }

    public abstract boolean getSettingsStatus();
}

