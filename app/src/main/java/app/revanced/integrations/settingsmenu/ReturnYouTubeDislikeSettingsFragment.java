package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislikeSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.RYD.getName());

        final Activity context = this.getActivity();

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        // RYD enable toggle
        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(SettingsEnum.RYD_ENABLED.getPath());
            preference.setDefaultValue(SettingsEnum.RYD_ENABLED.getDefaultValue());
            preference.setChecked(SettingsEnum.RYD_ENABLED.getBoolean());
            preference.setTitle(str("revanced_ryd_title"));
            preference.setSummary(str("revanced_ryd_summary"));
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                final boolean value = (Boolean) newValue;
                ReturnYouTubeDislike.onEnabledChange(value);
                SettingsEnum.RYD_ENABLED.saveValue(value);
                return true;
            });
        }

        // About category
        addAboutCategory(context, preferenceScreen);
    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("about"));

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("revanced_ryd_attribution_title"));
            preference.setSummary(str("revanced_ryd_attribution_summary"));
            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://returnyoutubedislike.com"));
                pref.getContext().startActivity(i);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle("GitHub");
            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/Anarios/return-youtube-dislike"));
                pref.getContext().startActivity(i);
                return false;
            });
        }
    }
}
