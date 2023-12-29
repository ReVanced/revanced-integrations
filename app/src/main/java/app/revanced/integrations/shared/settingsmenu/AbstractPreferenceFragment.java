package app.revanced.integrations.shared.settingsmenu;

import android.annotation.SuppressLint;
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

import static app.revanced.integrations.shared.StringRef.str;

/** @noinspection deprecation, DataFlowIssue , unused */
public abstract class AbstractPreferenceFragment extends PreferenceFragment {
    /**
     * The name of the shared preferences.
     */
    private final String prefName;

    /**
     * Indicates that if a preference changes,
     * to apply the change from the Setting to the UI component.
     */
    public static boolean settingImportInProgress;

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
                BooleanSetting boolSetting = (BooleanSetting) setting;
                if (settingImportInProgress) {
                    switchPref.setChecked(boolSetting.get());
                } else {
                    BooleanSetting.privateSetValue(boolSetting, switchPref.isChecked());
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
                updateListPreference(listPref, setting);
            } else {
                Logger.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }

            updatePreferencesAvailable();

            if (settingImportInProgress) {
                return;
            }

            if (!showingUserDialogMessage) {
                if (setting.userDialogMessage != null && ((SwitchPreference) pref).isChecked() != (Boolean) setting.defaultValue) {
                    showSettingUserDialogConfirmation((SwitchPreference) pref, (BooleanSetting) setting);
                } else if (setting.rebootApp) {
                    showRestartDialog(getContext());
                }
            }

        } catch (Exception ex) {
            Logger.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    public AbstractPreferenceFragment(String prefName) {
        this.prefName = prefName;
    }

    public AbstractPreferenceFragment() {
        this(Setting.DEFAULT_PREFERENCE_NAME);
    }

    protected void initialize() {
        final var identifier = Utils.getResourceIdentifier("revanced_prefs", "xml");

        if (identifier == 0) return;
        addPreferencesFromResource(identifier);

        updatePreferencesAvailable();
    }

    private void showSettingUserDialogConfirmation(SwitchPreference switchPref, BooleanSetting setting) {
        final var context = getContext();

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

    private void updatePreferencesAvailable() {
        for (Setting<?> setting : Setting.allLoadedSettings()) {
            Preference preference = findPreference(setting.key);
            if (preference != null) preference.setEnabled(setting.isAvailable());
        }
    }

    private static void updateListPreference(ListPreference listPreference, Setting<?> setting) {
        String objectStringValue = setting.get().toString();
        final int entryIndex = listPreference.findIndexOfValue(objectStringValue);
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
            listPreference.setValue(objectStringValue);
        } else {
            // Value is not an available option.
            // User manually edited import data, or options changed and current selection is no longer available.
            // Still show the value in the summary, so it's clear that something is selected.
            listPreference.setSummary(objectStringValue);
        }
    }

    public static void showRestartDialog(@NonNull final Context context) {
        String positiveButton = str("revanced_settings_restart");

        new AlertDialog.Builder(context).setMessage(str("revanced_settings_restart_title"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    Utils.restartApp(context);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .show();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(prefName);
            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

            initialize();
        } catch (Exception ex) {
            Logger.printException(() -> "onActivityCreated() failure", ex);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
