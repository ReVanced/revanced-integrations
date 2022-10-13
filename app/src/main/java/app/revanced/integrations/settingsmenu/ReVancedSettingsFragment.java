package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.videoplayer.AutoRepeat;
import app.revanced.integrations.videoplayer.Copy;
import app.revanced.integrations.videoplayer.CopyWithTimeStamp;
import app.revanced.integrations.videoplayer.DownloadButton;

public class ReVancedSettingsFragment extends PreferenceFragment {

    private List<PreferenceScreen> screens;

    private boolean Registered = false;
    private boolean settingsInitialized = false;

    private final CharSequence[] videoSpeedEntries = {"Auto", "0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x", "3x", "4x", "5x"};
    private final CharSequence[] videoSpeedentryValues = {"-2", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0"};
    private final CharSequence[] videoQualityEntries = {"Auto", "144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p"};
    private final CharSequence[] videoQualityentryValues = {"-2", "144", "240", "360", "480", "720", "1080", "1440", "2160"};
    //private final CharSequence[] buttonLocationEntries = {"None", "In player", "Under player", "Both"};
    //private final CharSequence[] buttonLocationentryValues = {"NONE", "PLAYER", "BUTTON_BAR", "BOTH"};

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        for (SettingsEnum setting : SettingsEnum.values()) {
            if (!setting.getPath().equals(str)) continue;
            Preference pref = this.findPreferenceOnScreen(str);

            LogHelper.debug(ReVancedSettingsFragment.class, "Setting " + setting.name() + " was changed. Preference " + str + ": " + pref.toString());

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                setting.setValue(switchPref.isChecked());

                if (setting == SettingsEnum.PREFERRED_AUTO_REPEAT) {
                    AutoRepeat.changeSelected(setting.getBoolean(), true);
                }

            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPref = (EditTextPreference) pref;
                Object value = null;
                switch (setting.getReturnType()) {
                    case FLOAT:
                        value = Float.parseFloat(editPref.getText());
                        break;
                    case LONG:
                        value = Long.parseLong(editPref.getText());
                        break;
                    case STRING:
                        value = editPref.getText();
                        break;
                    case INTEGER:
                        value = Integer.parseInt(editPref.getText());
                        break;
                    default:
                        LogHelper.printException(ReVancedSettingsFragment.class, "Setting has no valid return type! " + setting.getReturnType());
                        break;
                }
                setting.setValue(value);
            } else if (pref instanceof ListPreference) {
                Context context = ReVancedUtils.getContext();
                ListPreference listPref = (ListPreference) pref;
                if (setting == SettingsEnum.PREFERRED_VIDEO_SPEED) {
                    try {
                        String value = sharedPreferences.getString(setting.getPath(), setting.getDefaultValue() + "");
                        listPref.setDefaultValue(value);
                        listPref.setSummary(videoSpeedEntries[listPref.findIndexOfValue(String.valueOf(value))]);
                        SettingsEnum.PREFERRED_VIDEO_SPEED.saveValue(value);
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of speed" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting.toString());
                }

                if (setting == SettingsEnum.PREFERRED_VIDEO_QUALITY_WIFI) {
                    try {
                        String value = Integer.toString(SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "revanced_pref_video_quality_wifi", -2));
                        listPref.setDefaultValue(value);
                        listPref.setSummary(videoQualityEntries[listPref.findIndexOfValue(String.valueOf(value))]);
                        SettingsEnum.PREFERRED_VIDEO_QUALITY_WIFI.saveValue(Integer.parseInt(value));
                        SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "revanced_pref_video_quality_wifi", value + "");
                        Toast.makeText(context, "Changing default Wi-Fi quality to: " + SettingsEnum.PREFERRED_VIDEO_QUALITY_WIFI.getInt() + "p", Toast.LENGTH_SHORT).show();
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of wifi quality" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting.toString());
                }

                if (setting == SettingsEnum.PREFERRED_VIDEO_QUALITY_MOBILE) {
                    try {
                        String value = Integer.toString(SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "revanced_pref_video_quality_mobile", -2));
                        listPref.setDefaultValue(value);
                        listPref.setSummary(videoQualityEntries[listPref.findIndexOfValue(String.valueOf(value))]);
                        SettingsEnum.PREFERRED_VIDEO_QUALITY_MOBILE.saveValue(Integer.parseInt(value));
                        SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "revanced_pref_video_quality_mobile", value + "");
                        Toast.makeText(context, "Changing default mobile data quality to: " + SettingsEnum.PREFERRED_VIDEO_QUALITY_MOBILE.getInt() + "p", Toast.LENGTH_SHORT).show();
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of mobile quality" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting.toString());
                }

                if ("pref_copy_video_url_timestamp_button_list".equals(str)) {
                    CopyWithTimeStamp.refreshShouldBeShown();
                } else if ("pref_copy_video_url_button_list".equals(str)) {
                    Copy.refreshShouldBeShown();
                } else if ("pref_download_button_list".equals(str)) {
                    DownloadButton.refreshShouldBeShown();
                }
            } else {
                LogHelper.printException(ReVancedSettingsFragment.class, "Setting cannot be handled! " + pref.toString());
            }

            if (ReVancedUtils.getContext() != null && settingsInitialized && setting.shouldRebootOnChange()) {
                rebootDialog(getActivity());
            }
        }
    };

    @SuppressLint("ResourceType")
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.YOUTUBE.getName());
        try {
            int identifier = getResources().getIdentifier("revanced_prefs", "xml", getPackageName());
            addPreferencesFromResource(identifier);

            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            this.settingsInitialized = sharedPreferences.getBoolean("revanced_initialized", false);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this.listener);
            this.Registered = true;

            this.screens = new ArrayList<>();
            this.screens.add((PreferenceScreen) getPreferenceScreen().findPreference("revanced_pref_default_video_quality"));

            final ListPreference wifiQualityList = (ListPreference) screens.get(0).findPreference("revanced_pref_video_quality_wifi");
            setListPreferenceData(wifiQualityList, true);

            wifiQualityList.setOnPreferenceClickListener(preference -> {
                setListPreferenceData(wifiQualityList, true);
                return false;
            });

            final ListPreference mobileQualityList = (ListPreference) screens.get(0).findPreference("revanced_pref_video_quality_mobile");
            setListPreferenceData(mobileQualityList, false);

            mobileQualityList.setOnPreferenceClickListener(preference -> {
                setListPreferenceData(mobileQualityList, false);
                return false;
            });

            sharedPreferences.edit().putBoolean("revanced_initialized", true);
            this.settingsInitialized = true;
        } catch (Throwable th) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Error during onCreate()", th);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }

    private Preference findPreferenceOnScreen(CharSequence key) {
        if (key == null) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Key cannot be null!");
            return null;
        }
        Preference pref = null;
        if (this.findPreference(key) != null) {
            pref = this.findPreference(key);
        } else {
            for (PreferenceScreen screen : this.screens) {
                Preference toCheck = screen.findPreference(key);
                if (toCheck == null) continue;
                pref = toCheck;
                LogHelper.debug(ReVancedSettingsFragment.class, "Found preference " + key + " on screen: " + screen.getTitle());
            }
        }

        return pref;
    }

    private void setListPreferenceData(ListPreference listPreference, boolean z) {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        String value = sharedPreferences.getString(z ? "revanced_pref_video_quality_wifi" : "revanced_pref_video_quality_mobile", "-2");
        if (listPreference.getValue() == null) {
            listPreference.setValue(value);
        }
        listPreference.setEntries(this.videoQualityEntries);
        listPreference.setEntryValues(this.videoQualityentryValues);
        listPreference.setSummary(this.videoQualityEntries[listPreference.findIndexOfValue(value)]);
    }

    /*
    private void setCopyLinkListPreferenceData(ListPreference listPreference, String str) {
        listPreference.setEntries(this.buttonLocationEntries);
        listPreference.setEntryValues(this.buttonLocationentryValues);
        String string = this.sharedPreferences.getString(str, "NONE");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.buttonLocationEntries[listPreference.findIndexOfValue(string)]);
    }
    */

    private String getPackageName() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Context is null, returning com.google.android.youtube!");
            return "com.google.android.youtube";
        }
        String PACKAGE_NAME = context.getPackageName();
        LogHelper.debug(ReVancedSettingsFragment.class, "getPackageName: " + PACKAGE_NAME);

        return PACKAGE_NAME;
    }

    private void reboot(Activity activity, Class homeActivityClass) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, Shell_HomeActivity.class), intent));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).setMessage(getStringByName(activity, "pref_refresh_config")).setPositiveButton(getStringByName(activity, "in_app_update_restart_button"), (dialog, id) -> reboot(activity, Shell_HomeActivity.class)).setNegativeButton(getStringByName(activity, "sign_in_cancel"), null).show();
    }

    private String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            LogHelper.printException(ReVancedUtils.class, "Resource not found.", exception);
            return "";
        }
    }

}
