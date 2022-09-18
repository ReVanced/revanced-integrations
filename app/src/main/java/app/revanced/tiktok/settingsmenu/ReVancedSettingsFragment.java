package app.revanced.tiktok.settingsmenu;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.utils.SharedPrefHelper;

public class ReVancedSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS.getName());

        final Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        //Feed filter
        if (SettingsStatus.feedFilter) {
            PreferenceCategory feedFilter = new PreferenceCategory(context);
            feedFilter.setTitle("Feed filter");
            preferenceScreen.addPreference(feedFilter);

            //Remove ads toggle
            {
                SwitchPreference preference = new SwitchPreference(context);
                feedFilter.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_REMOVE_ADS.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_REMOVE_ADS.getDefaultValue());
                preference.setChecked(SettingsEnum.TIK_REMOVE_ADS.getBoolean());
                preference.setTitle("Remove feed ads");
                preference.setSummary("Remove ads from feed.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_REMOVE_ADS.saveValue(value);
                    return true;
                });
            }
            //Hide LiveStreams toggle
            {
                SwitchPreference preference = new SwitchPreference(context);
                feedFilter.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_HIDE_LIVE.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_HIDE_LIVE.getDefaultValue());
                preference.setChecked(SettingsEnum.TIK_HIDE_LIVE.getBoolean());
                preference.setTitle("Hide livestreams");
                preference.setSummary("Hide livestreams from feed.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_HIDE_LIVE.saveValue(value);
                    return true;
                });
            }
        }

        //Integration
        PreferenceCategory integration = new PreferenceCategory(context);
        integration.setTitle("Integration");
        preferenceScreen.addPreference(integration);
        //Enable DebugLog toggle
        {
            SwitchPreference preference = new SwitchPreference(context);
            integration.addPreference(preference);
            preference.setKey(SettingsEnum.TIK_DEBUG.getPath());
            preference.setDefaultValue(SettingsEnum.TIK_DEBUG.getDefaultValue());
            preference.setChecked(SettingsEnum.TIK_DEBUG.getBoolean());
            preference.setTitle("Enable debug log");
            preference.setSummary("Show integration debug log.");
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                final boolean value = (Boolean) newValue;
                SettingsEnum.TIK_DEBUG.saveValue(value);
                return true;
            });
        }
    }
}
