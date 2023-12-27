package app.revanced.integrations.tiktok.settingsmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.tiktok.settings.SharedPrefCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.integrations.tiktok.settingsmenu.preference.RangeValuePreference;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.DownloadsPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.FeedFilterPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.IntegrationsPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.SimSpoofPreferenceCategory;

@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends PreferenceFragment {
    private boolean registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            Setting<?> setting = Setting.getSettingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                BooleanSetting.privateSetValue((BooleanSetting) setting, switchPref.isChecked());
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPreference = (EditTextPreference) pref;
                Setting.privateSetValueFromString(setting, editPreference.getText());
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                Setting.privateSetValueFromString(setting, listPref.getValue());
                Setting.setListPreference((ListPreference) pref, setting);
            } else if (pref instanceof RangeValuePreference) {
                RangeValuePreference rangeValuePref = (RangeValuePreference) pref;
                Setting.privateSetValueFromString(setting, rangeValuePref.getValue());
            } else if (pref instanceof DownloadPathPreference) {
                DownloadPathPreference downloadPathPref = (DownloadPathPreference) pref;
                Setting.privateSetValueFromString(setting, downloadPathPref.getValue());
            } else {
                Logger.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }
            if (Utils.getContext() != null && this.settingsInitialized && setting.rebootApp) {
                rebootDialog(getActivity());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registered = true;

        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.TIKTOK_PREFS.prefName);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);

        final Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new IntegrationsPreferenceCategory(context, preferenceScreen);

        this.settingsInitialized = true;
    }

    @Override
    public void onDestroy() {
        if (this.registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.registered = false;
        }

        super.onDestroy();
    }

    private void rebootDialog(@NonNull Context context) {
        new AlertDialog.Builder(context).setMessage("Refresh and restart")
                .setPositiveButton("Restart", (dialog, i) -> app.revanced.integrations.shared.Utils.restartApp(context))
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
