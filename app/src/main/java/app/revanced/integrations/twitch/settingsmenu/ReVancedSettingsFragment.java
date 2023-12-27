package app.revanced.integrations.twitch.settingsmenu;

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
import app.revanced.integrations.shared.settings.Setting;

import static app.revanced.integrations.shared.StringRef.str;

/** @noinspection deprecation*/
public class ReVancedSettingsFragment extends PreferenceFragment {

    private boolean registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        Logger.printDebug(() -> "Setting '" + key + "' changed");
        syncPreference(key);
    };

    /**
     * Sync preference
     * @param key Preference to load. If key is null, all preferences are updated
     */
    private void syncPreference(@Nullable String key) {
        if (key == null) {
            Setting.setPreferences(this);
            // First onChange event is caused by initial state loading
            this.settingsInitialized = true;
            return;
        }

        Setting setting = Setting.getSettingFromPath(key);
        if (setting == null) return;

        Logger.printDebug(() -> "Syncing setting '" + setting.key + "' with UI");

        setting.setPreference(this);

        if (Utils.getContext() != null && settingsInitialized && setting.rebootApp)
            showRestartDialog(getContext());
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName("revanced_prefs");
            manager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);

            addPreferencesFromResource(
                    getResources().getIdentifier(
                            "revanced_prefs",
                            "xml",
                            this.getContext().getPackageName()
                    )
            );

            // TODO: remove this line.  On load the UI should apply the values from Settings using the code above.
            // It should not apply the UI values to the Settings here
            syncPreference(null);

            this.registered = true;
        } catch (Throwable th) {
            Logger.printException(() -> "Error during onCreate()", th);
        }
    }

    @Override
    public void onDestroy() {
        if (this.registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.registered = false;
        }
        super.onDestroy();
    }

    private void showRestartDialog(@NonNull Context context) {
        new AlertDialog.Builder(context).
                setMessage(str("revanced_reboot_message")).
                setPositiveButton(str("revanced_reboot"),
                        (dialog, i) -> Utils.restartApp(context))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
