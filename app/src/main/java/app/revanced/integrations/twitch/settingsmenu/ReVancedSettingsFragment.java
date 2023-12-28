package app.revanced.integrations.twitch.settingsmenu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import static app.revanced.integrations.shared.StringRef.str;

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
            Setting setting = Setting.getSettingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            Logger.printDebug(() -> setting.key + ": " + " setting value:" + setting.getObjectValue()  + " pref:" + pref);
            if (pref == null) {
                return;
            }

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                if (settingImportInProgress) {
                    switchPref.setChecked(setting.getBoolean());
                } else {
                    setting.setValue(switchPref.isChecked());
                }
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPreference = (EditTextPreference) pref;
                if (settingImportInProgress) {
                    editPreference.getEditText().setText(setting.getObjectValue().toString());
                } else {
                    setting.setValue(editPreference.getText());
                }
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                if (settingImportInProgress) {
                    listPref.setValue(setting.getObjectValue().toString());
                } else {
                    setting.setValue(listPref.getValue());
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
                    showSettingUserDialogConfirmation(getContext(), (SwitchPreference) pref, setting);
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };


    @SuppressLint("ResourceType")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName("revanced_prefs");
            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

            addPreferencesFromResource(Utils.getResourceIdentifier("revanced_prefs", "xml"));

            Setting.setPreferencesEnabled(this);

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

    static void showRestartDialog(@NonNull Context context) {
        String positiveButton = str("in_app_update_restart_button");
        new AlertDialog.Builder(context).setMessage(str("pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    Utils.restartApp(context);
                })
                .setNegativeButton(android.R.string.cancel,  null)
                .setCancelable(false)
                .show();
    }

    private void showSettingUserDialogConfirmation(@NonNull Context context, SwitchPreference switchPref, Setting setting) {
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
                    Boolean defaultBooleanValue = (Boolean) setting.defaultValue;
                    setting.setValue(defaultBooleanValue);
                    switchPref.setChecked(defaultBooleanValue);
                })
                .setOnDismissListener(dialog -> showingUserDialogMessage = false)
                .setCancelable(false)
                .show();
    }
}
