package app.revanced.integrations.youtube.settingsmenu;

import static app.revanced.integrations.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.settings.SharedPrefCategory;

/** @noinspection deprecation*/
public class ReVancedSettingsFragment extends PreferenceFragment {
    /**
     * Indicates that if a preference changes,
     * to apply the change from the Setting to the UI component.
     */
    static boolean settingImportInProgress;

    /**
     * Used to prevent showing reboot dialog, if user cancels a setting user dialog.
     */
    private boolean showingUserDialogMessage;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            Setting<?> setting = Setting.getSettingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            Logger.printDebug(() -> setting.key + ": " + " setting value:" + setting.get() + " pref:" + pref);
            if (pref == null) {
                return;
            }

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                if (settingImportInProgress) {
                    switchPref.setChecked(((BooleanSetting) setting).get());
                } else {
                    BooleanSetting.privateSetValue((BooleanSetting) setting, switchPref.isChecked());
                }
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPreference = (EditTextPreference) pref;
                if (settingImportInProgress) {
                    editPreference.getEditText().setText(setting.get().toString());
                } else {
                    Setting.privateSetValueFromString(setting, editPreference.getText());
                }
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                if (settingImportInProgress) {
                    listPref.setValue(setting.get().toString());
                } else {
                    Setting.privateSetValueFromString(setting, listPref.getValue());
                }
                Setting.setListPreference(listPref, setting);
            } else {
                Logger.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }

            Setting.setPreferencesEnabled(this);

            if (settingImportInProgress) {
                return;
            }

            if (!showingUserDialogMessage) {
                if (setting.userDialogMessage != null && ((SwitchPreference) pref).isChecked() != (Boolean) setting.defaultValue) {
                    showSettingUserDialogConfirmation(getContext(), (SwitchPreference) pref, (BooleanSetting) setting);
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    static void showRestartDialog(@NonNull Context contxt) {
        String positiveButton = str("in_app_update_restart_button");
        new AlertDialog.Builder(contxt).setMessage(str("pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    Utils.restartApp(contxt);
                })
                .setNegativeButton(android.R.string.cancel,  null)
                .setCancelable(false)
                .show();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(SharedPrefCategory.YOUTUBE.prefName);
            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

            addPreferencesFromResource(Utils.getResourceIdentifier("revanced_prefs", "xml"));

            Setting.setPreferencesEnabled(this);

            // if the preference was included, then initialize it based on the available playback speed
            Preference defaultSpeedPreference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
            if (defaultSpeedPreference instanceof ListPreference) {
                CustomPlaybackSpeedPatch.initializeListPreference((ListPreference) defaultSpeedPreference);
            }

            // Set the preference values to the current setting values.
            Setting.setPreferences(this);

        } catch (Exception ex) {
            Logger.printException(() -> "onActivityCreated() failure", ex);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    private void showSettingUserDialogConfirmation(@NonNull Context context, SwitchPreference switchPref, BooleanSetting setting) {
        showingUserDialogMessage = true;
        new AlertDialog.Builder(context)
                .setTitle(str("revanced_settings_confirm_user_dialog_title"))
                .setMessage(setting.userDialogMessage.toString())
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (setting.rebootApp) {
                        showRestartDialog(context);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    Boolean defaultBooleanValue = setting.defaultValue;
                    BooleanSetting.privateSetValue(setting, defaultBooleanValue);
                    switchPref.setChecked(defaultBooleanValue);
                })
                .setOnDismissListener(dialog -> {
                    showingUserDialogMessage = false;
                })
                .setCancelable(false)
                .show();
    }
}
