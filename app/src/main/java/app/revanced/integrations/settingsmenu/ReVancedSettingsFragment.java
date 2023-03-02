package app.revanced.integrations.settingsmenu;

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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefCategory;

public class ReVancedSettingsFragment extends PreferenceFragment {
    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            for (SettingsEnum setting : SettingsEnum.values()) {
                if (!setting.path.equals(str)) continue;
                Preference pref = this.findPreference(str);

                LogHelper.printDebug(() -> "Setting " + setting.name() + " was changed. Preference '" + str + "': " + pref);

                if (pref instanceof SwitchPreference) {
                    SwitchPreference switchPref = (SwitchPreference) pref;
                    SettingsEnum.setValue(setting, switchPref.isChecked());
                } else if (pref instanceof EditTextPreference) {
                    EditTextPreference editPref = (EditTextPreference) pref;
                    Object value;
                    switch (setting.returnType) {
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
                            throw new IllegalStateException(setting.toString());
                    }
                    SettingsEnum.setValue(setting, value);
                } else {
                    LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                }

                if (ReVancedUtils.getContext() != null && setting.rebootApp) {
                    rebootDialog(getActivity());
                }
            }

            turnOnOffPreferences();
        } catch (Exception ex) {
            LogHelper.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    @SuppressLint("ResourceType")
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.YOUTUBE.prefName);
        try {
            String packageName = ReVancedUtils.getContext().getPackageName();
            final int identifier = getResources().getIdentifier("revanced_prefs", "xml", packageName);
            addPreferencesFromResource(identifier);

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        } catch (Exception ex) {
            LogHelper.printException(() -> "onCreate() error", ex);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        turnOnOffPreferences();
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    private void turnOnOffPreferences() {
        for (SettingsEnum setting : SettingsEnum.values()) {
            Preference preference = this.findPreference(setting.path);
            if (preference != null) {
                preference.setEnabled(setting.isAvailable());
            }
        }
    }

    private void reboot(Activity activity) {
        final int intentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, Shell_HomeActivity.class), intentFlags);
        AlarmManager systemService = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        systemService.setExact(AlarmManager.ELAPSED_REALTIME, 1500L, intent);
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(Activity activity) {
        String positiveButton = getStringByName(activity, "in_app_update_restart_button");
        String negativeButton = getStringByName(activity, "sign_in_cancel");
        new AlertDialog.Builder(activity).setMessage(getStringByName(activity, "pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> reboot(activity))
                .setNegativeButton(negativeButton, null).show();
    }

    private String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            LogHelper.printException(() -> "Resource not found.", exception);
            return "";
        }
    }

}
