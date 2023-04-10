package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.annotation.SuppressLint;
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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReVancedSettingsFragment extends PreferenceFragment {
    private final CharSequence[] videoSpeedEntries = {"Auto", "0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x", "3x", "4x", "5x"};
    private final CharSequence[] videoSpeedentryValues = {"-2", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0"};
    private final CharSequence[] videoQualityEntries = {"Auto", "144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p"};
    private final CharSequence[] videoQualityEntryValues = {"-2", "144", "240", "360", "480", "720", "1080", "1440", "2160"};

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            SettingsEnum setting = SettingsEnum.settingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = this.findPreference(str);
            LogHelper.printDebug(() -> "Setting " + setting.name() + " was changed. Preference " + str + ": " + pref);

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                SettingsEnum.setValue(setting, switchPref.isChecked());
            } else if (pref instanceof EditTextPreference) {
                String editText = ((EditTextPreference) pref).getText();
                Object value;
                switch (setting.returnType) {
                    case INTEGER:
                        value = Integer.parseInt(editText);
                        break;
                    case LONG:
                        value = Long.parseLong(editText);
                        break;
                    case FLOAT:
                        value = Float.parseFloat(editText);
                        break;
                    case STRING:
                        value = editText;
                        break;
                    default:
                        throw new IllegalStateException(setting.toString());
                }
                SettingsEnum.setValue(setting, value);
            } else if (pref instanceof ListPreference) {
                Context context = ReVancedUtils.getContext();
                ListPreference listPref = (ListPreference) pref;
                if (setting == SettingsEnum.PREFERRED_VIDEO_SPEED) {
                    try {
                        String value = sharedPreferences.getString(setting.getPath(), setting.getDefaultValue() + "");
                        listPref.setDefaultValue(value);
                        listPref.setSummary(videoSpeedEntries[listPref.findIndexOfValue(value)]);
                        SettingsEnum.PREFERRED_VIDEO_SPEED.saveValue(value);
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of speed" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting.toString());
                }

                if (setting == SettingsEnum.DEFAULT_VIDEO_QUALITY_WIFI) {
                    try {
                        updateVideoQuality(context, listPref, setting, false);
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of wifi quality" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting);
                }

                if (setting == SettingsEnum.DEFAULT_VIDEO_QUALITY_MOBILE) {
                    try {
                        updateVideoQuality(context, listPref, setting, false);
                    } catch (Throwable th) {
                        LogHelper.printException(ReVancedSettingsFragment.class, "Error setting value of mobile quality" + th);
                    }
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting);
                }

                if ("pref_download_button_list".equals(str)) {
                    DownloadButton.refreshShouldBeShown();
                }
            } else {
                LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
            }

            if (setting.userDialogMessage != null && ((SwitchPreference) pref).isChecked() != (Boolean) setting.defaultValue) {
                showSettingUserDialogConfirmation(getActivity(), (SwitchPreference) pref, setting);
            } else if (setting.rebootApp) {
                rebootDialog(getActivity());
            }

            enableDisablePreferences();
        } catch (Exception ex) {
            LogHelper.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    @SuppressLint("ResourceType")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(SharedPrefCategory.YOUTUBE.prefName);
            addPreferencesFromResource(ReVancedUtils.getResourceIdentifier("revanced_prefs", "xml"));

            enableDisablePreferences();

            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        } catch (Exception ex) {
            LogHelper.printException(() -> "onActivityCreated() error", ex);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    private void enableDisablePreferences() {
        for (SettingsEnum setting : SettingsEnum.values()) {
            Preference preference = this.findPreference(setting.path);
            if (preference != null) {
                preference.setEnabled(setting.isAvailable());
            }
        }
    }

    private void updateVideoQuality(Context context, ListPreference listPref, SettingsEnum setting, Boolean auto) {
        String key = setting.getPath();
        String value = Integer.toString(SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, key, -2));
        listPref.setDefaultValue(value);
        listPref.setSummary(listPref.getEntry());
        setting.saveValue(Integer.parseInt(value));
        SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, key, value + "");

        if (setting.getInt() == Integer.parseInt((String) videoQualityEntryValues[0]))
            auto = true;
        String network = key == SettingsEnum.DEFAULT_VIDEO_QUALITY_WIFI.getPath() ? " Wi-Fi " : " mobile ";
        String qualityValue = auto ? "Auto" : setting.getInt() + "p";
        Toast.makeText(context, "Changing default" + network + "quality to: " + qualityValue, Toast.LENGTH_SHORT).show();
    }

    private void reboot(@NonNull Activity activity) {
        final int intentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, Shell_HomeActivity.class), intentFlags);
        AlarmManager systemService = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        systemService.setExact(AlarmManager.ELAPSED_REALTIME, 1500L, intent);
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(@NonNull Activity activity) {
        String positiveButton = str("in_app_update_restart_button");
        String negativeButton = str("sign_in_cancel");
        new AlertDialog.Builder(activity).setMessage(str("pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    reboot(activity);
                })
                .setNegativeButton(negativeButton,  null)
                .show();
    }

    private void showSettingUserDialogConfirmation(@NonNull Activity activity, SwitchPreference switchPref, SettingsEnum setting) {
        new AlertDialog.Builder(activity)
                .setTitle(str("revanced_settings_confirm_user_dialog_title"))
                .setMessage(setting.userDialogMessage.toString())
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (setting.rebootApp) {
                        rebootDialog(activity);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    Boolean defaultBooleanValue = (Boolean) setting.defaultValue;
                    SettingsEnum.setValue(setting, defaultBooleanValue);
                    switchPref.setChecked(defaultBooleanValue);
                })
                .show();
    }
}
