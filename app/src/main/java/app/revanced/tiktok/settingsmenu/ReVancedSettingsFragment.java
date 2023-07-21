package app.revanced.tiktok.settingsmenu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.ss.android.ugc.aweme.splash.SplashActivity;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settings.SharedPrefCategory;
import app.revanced.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.tiktok.settingsmenu.preference.InputTextPreference;
import app.revanced.tiktok.settingsmenu.preference.RangeValuePreference;
import app.revanced.tiktok.settingsmenu.preference.TogglePreference;
import app.revanced.tiktok.utils.ReVancedUtils;

public class ReVancedSettingsFragment extends PreferenceFragment {

    private boolean Registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            SettingsEnum setting = SettingsEnum.settingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                SettingsEnum.setValue(setting, switchPref.isChecked());
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPreference = (EditTextPreference) pref;
                SettingsEnum.setValue(setting, editPreference.getText());
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                SettingsEnum.setValue(setting, listPref.getValue());
                updateListPreferenceSummary((ListPreference) pref, setting);
            } else if (pref instanceof RangeValuePreference) {
                RangeValuePreference rangeValuePref = (RangeValuePreference) pref;
                SettingsEnum.setValue(setting, rangeValuePref.getValue());
            } else if (pref instanceof DownloadPathPreference) {
                DownloadPathPreference downloadPathPref = (DownloadPathPreference) pref;
                SettingsEnum.setValue(setting, downloadPathPref.getValue());
            } else {
                LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }
            if (ReVancedUtils.getAppContext() != null && this.settingsInitialized && setting.rebootApp) {
                rebootDialog(getActivity());
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    private void updateListPreferenceSummary(ListPreference listPreference, SettingsEnum setting) {
        String objectStringValue = setting.getObjectValue().toString();
        final int entryIndex = listPreference.findIndexOfValue(objectStringValue);
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
            listPreference.setValue(objectStringValue);
        } else {
            listPreference.setSummary(objectStringValue);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.TIKTOK_PREFS.prefName);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);
        this.Registered = true;

        final Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        //Feed filter
        if (SettingsStatus.feedFilter) {
            PreferenceCategory feedFilter = new PreferenceCategory(context);
            feedFilter.setTitle("Feed filter");
            preferenceScreen.addPreference(feedFilter);
            feedFilter.addPreference(new TogglePreference(context, "Remove feed ads", "Remove ads from feed.", SettingsEnum.TIK_REMOVE_ADS));
            feedFilter.addPreference(new TogglePreference(context, "Hide livestreams", "Hide livestreams from feed.", SettingsEnum.TIK_HIDE_LIVE));
            feedFilter.addPreference(new TogglePreference(context, "Hide story", "Hide story from feed.", SettingsEnum.TIK_HIDE_STORY));
            feedFilter.addPreference(new TogglePreference(context, "Hide image video", "Hide image video from feed.", SettingsEnum.TIK_HIDE_IMAGE));
            feedFilter.addPreference(new RangeValuePreference(context, "View count", "If app show error, please change value or refresh few times", SettingsEnum.TIK_HIDE_PLAY_COUNT));
            feedFilter.addPreference(new RangeValuePreference(context, "Like count", "If app show error, please change value or refresh few times", SettingsEnum.TIK_HIDE_LIKE_COUNT));
        }

        //Download
        if (SettingsStatus.download) {
            PreferenceCategory download = new PreferenceCategory(context);
            download.setTitle("Download");
            preferenceScreen.addPreference(download);
            download.addPreference(new DownloadPathPreference(context, "Download path", SettingsEnum.TIK_DOWN_PATH));
            download.addPreference(new TogglePreference(context, "Remove watermark", "", SettingsEnum.TIK_DOWN_WATERMARK));
        }

        // SpoofSimPatch
        if (SettingsStatus.simSpoof) {
            PreferenceCategory simSpoof = new PreferenceCategory(context);
            simSpoof.setTitle("Bypass regional restriction");
            preferenceScreen.addPreference(simSpoof);
            simSpoof.addPreference(new TogglePreference(context, "Fake sim card info", "Bypass regional restriction by fake sim card information.", SettingsEnum.TIK_SIMSPOOF));
            simSpoof.addPreference(new InputTextPreference(context, "Country ISO", "us, uk, jp, ...", SettingsEnum.TIK_SIMSPOOF_ISO));
            simSpoof.addPreference(new InputTextPreference(context, "Operator mcc+mnc", "mcc+mnc", SettingsEnum.TIK_SIMSPOOF_MCCMNC));
            simSpoof.addPreference(new InputTextPreference(context, "Operator name", "Name of the operator", SettingsEnum.TIK_SIMSPOOF_OP_NAME));
        }

        //Integration
        PreferenceCategory integration = new PreferenceCategory(context);
        integration.setTitle("Integration");
        preferenceScreen.addPreference(integration);
        integration.addPreference(new TogglePreference(context, "Enable debug log", "Show integration debug log.", SettingsEnum.TIK_DEBUG));
        this.settingsInitialized = true;
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }

    private void reboot(Activity activity) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, SplashActivity.class), intent));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).setMessage("Refresh and restart").setPositiveButton("RESTART", (dialog, i) -> reboot(activity)).setNegativeButton("CANCEL", null).show();
    }
}
