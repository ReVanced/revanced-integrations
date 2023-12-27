package app.revanced.integrations.shared.settings;

import android.content.Context;
import android.preference.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.youtube.settings.SharedPrefCategory;
import app.revanced.integrations.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.StringRef;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static app.revanced.integrations.shared.StringRef.str;

public abstract class Setting<T> {
    /**
     * All settings that were instantiated.
     * When a new setting is created, it is automatically added to this list.
     */
    private static final List<Setting<?>> SETTINGS = new LinkedList<>();

    /**
     * Map of setting path to setting object.
     */
    private static final Map<String, Setting<?>> PATH_TO_SETTINGS = new HashMap<>();

    /**
     * Currently only Boolean types can be parents.
     */
    public static BooleanSetting[] getParents(BooleanSetting... parents) {
        return parents;
    }

    @Nullable
    public static Setting<?> getSettingFromPath(@NonNull String str) {
        return PATH_TO_SETTINGS.get(str);
    }

    /**
     * The key used to store the value in the shared preferences.
     */
    @NonNull
    public final String key;

    /**
     * The default value of the setting.
     */
    @NonNull
    public final T defaultValue;

    /**
     * The category of the shared preferences to store the value in.
     */
    @NonNull
    public final SharedPrefCategory sharedPrefCategory;

    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;

    /**
     * If this setting should be included when importing/exporting settings.
     */
    public final boolean includeWithImportExport;

    /**
     * Set of boolean parent settings.
     * If any of the parents are enabled, then this setting is available to configure.
     */
    @Nullable
    private final BooleanSetting[] parents;

    /**
     * Confirmation message to display, if the user tries to change the setting from the default value.
     */
    @Nullable
    public final StringRef userDialogMessage;

    // Must be volatile, as some settings are read/write from different threads.
    // Of note, the object value is persistently stored using SharedPreferences (which is thread safe).
    /**
     * The value of the setting.
     */
    @NonNull
    protected volatile T value;

    public Setting(String key, T defaultValue) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, false, true, null, null);
    }
    public Setting(String key, T defaultValue,
                   boolean rebootApp) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, true, null, null);
    }
    public Setting(@NonNull String key, @NonNull T defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, includeWithImportExport);
    }
    public Setting(String key, T defaultValue,
                   String userDialogMessage) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, false, true, userDialogMessage, null);
    }
    public Setting(String key, T defaultValue,
                   BooleanSetting[] parents) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, false, true, null, parents);
    }
    public Setting(String key, T defaultValue,
                   boolean rebootApp, String userDialogMessage) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, true, userDialogMessage, null);
    }
    public Setting(String key, T defaultValue,
                   boolean rebootApp, BooleanSetting[] parents) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, true, null, parents);
    }
    public Setting(String key, T defaultValue,
                   boolean rebootApp, String userDialogMessage, BooleanSetting[] parents) {
        this(key, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, true, userDialogMessage, parents);
    }
    public Setting(String key, T defaultValue, SharedPrefCategory prefName) {
        this(key, defaultValue, prefName, false, true, null, null);
    }
    public Setting(String key, T defaultValue, SharedPrefCategory prefName,
                   boolean rebootApp) {
        this(key, defaultValue, prefName, rebootApp, true, null, null);
    }
    public Setting(String key, T defaultValue, SharedPrefCategory prefName,
                   String userDialogMessage) {
        this(key, defaultValue, prefName, false, true, userDialogMessage, null);
    }
    public Setting(String key, T defaultValue, SharedPrefCategory prefName,
                   BooleanSetting[] parents) {
        this(key, defaultValue, prefName, false, true, null, parents);
    }
    public Setting(@NonNull String key, @NonNull T defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport) {
        this(key, defaultValue, prefName, rebootApp, includeWithImportExport, null, null);
    }

    /**
     * A setting backed by a shared preference.
     * @param key The key used to store the value in the shared preferences.
     * @param defaultValue The default value of the setting.
     * @param prefName The category of the shared preferences to store the value in.
     * @param rebootApp If the app should be rebooted, if this setting is changed.
     * @param includeWithImportExport If this setting should be shown in the import/export dialog.
     * @param userDialogMessage Confirmation message to display, if the user tries to change the setting from the default value.
     * @param parents Set of boolean parent settings that must be enabled, for this setting to be available to configure.
     */
    public Setting(@NonNull String key,
                   @NonNull T defaultValue,
                   @NonNull SharedPrefCategory prefName,
                   boolean rebootApp,
                   boolean includeWithImportExport,
                   @Nullable String userDialogMessage,
                   @Nullable BooleanSetting[] parents
    ) {
        this.key = key;
        this.value = this.defaultValue = defaultValue;
        this.sharedPrefCategory = prefName;
        this.rebootApp = rebootApp;
        this.includeWithImportExport = includeWithImportExport;
        this.userDialogMessage = (userDialogMessage == null) ? null : new StringRef(userDialogMessage);
        this.parents = parents;

        load();

        SETTINGS.add(this);
        PATH_TO_SETTINGS.put(key, this);
    }

    /**
     * Migrate a setting value if the path is renamed but otherwise the old and new settings are identical.
     */
    public static void migrateOldSettingToNew(Setting oldSetting, Setting newSetting) {
        if (!oldSetting.isSetToDefault()) {
            Logger.printInfo(() -> "Migrating old setting of '" + oldSetting.value
                    + "' from: " + oldSetting + " into replacement setting: " + newSetting);
            newSetting.save(oldSetting.value);
            oldSetting.resetToDefault();
        }
    }

    /**
     * Sets, but does _not_ persistently save the value.
     * This method is only to be used by the Settings preference code.
     *
     * This intentionally is a static method to deter
     * accidental usage when {@link #save(Object)} was intended.
     */
    public static void privateSetValueFromString(@NonNull Setting setting, @NonNull String newValue) {
        setting.setValueFromString(newValue);
    }

    /**
     * Sets the value of {@link #value}, but do not save to {@link #sharedPrefCategory}.
     */
    protected abstract void setValueFromString(@NonNull String newValue);

    /**
     * Load and set the value of {@link #value}.
     */
    protected abstract void load();

    /**
     * Persistently saves the value.
     */
    public abstract void save(@NonNull T newValue);

    @NonNull
    public abstract T get();

    /**
     * Identical to calling {@link #save(Object)} using {@link #defaultValue}.
     */
    public void resetToDefault() {
        save(defaultValue);
    }

    /**
     * @return if this setting can be configured and used.
     */
    public boolean isAvailable() {
        if (parents == null) {
            return true;
        }
        for (BooleanSetting parent : parents) {
            if (parent.get()) return true;
        }
        return false;
    }

    /**
     * @return if the currently set value is the same as {@link #defaultValue}
     */
    public boolean isSetToDefault() {
        return value.equals(defaultValue);
    }

    /** @noinspection deprecation*/
    public static void setPreferencesEnabled(PreferenceFragment fragment) {
        for (Setting setting : SETTINGS) {
            Preference preference = fragment.findPreference(setting.key);
            if (preference != null) preference.setEnabled(setting.isAvailable());
        }
    }

    /** @noinspection deprecation*/
    public static void setPreferences(PreferenceFragment fragment) {
        for (Setting setting : SETTINGS) setting.setPreference(fragment);
    }

    /** @noinspection deprecation*/
    public void setPreference(PreferenceFragment fragment) {
        Preference preference = fragment.findPreference(key);
        if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(((Setting<Boolean>) this).get());
        } else if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).setText(get().toString());
        } else if (preference instanceof ListPreference) {
            setListPreference((ListPreference) preference, this);
        }
    }

    /** @noinspection deprecation*/
    public static void setListPreference(ListPreference listPreference, Setting setting) {
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

    @NotNull
    @Override
    public String toString() {
        return key + "=" + get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Setting)) return false;
        return key.equals(((Setting) obj).key);
    }

    // region Import / export

    /**
     * If a setting path has this prefix, then remove it before importing/exporting.
     */
    private static final String OPTIONAL_REVANCED_SETTINGS_PREFIX = "revanced_";

    /**
     * The path, minus any 'revanced' prefix to keep json concise.
     */
    private String getImportExportKey() {
        if (key.startsWith(OPTIONAL_REVANCED_SETTINGS_PREFIX)) {
            return key.substring(OPTIONAL_REVANCED_SETTINGS_PREFIX.length());
        }
        return key;
    }

    /**
     * @return the value stored using the import/export key.  Do not set any values in this method.
     */
    protected abstract T readFromJSON(JSONObject json, String importExportKey) throws JSONException;

    /**
     * Saves this instance to JSON.
     */
    protected void writeToJSON(JSONObject json, String importExportKey) throws JSONException {
        json.put(importExportKey, value);
    }

    private static Setting<?>[] sortSettingsByValueForExport() {
        Setting<?>[] sorted = SETTINGS.toArray(new Setting<?>[0]);

        // TODO: Figure out an object oriented way to do this.
        Arrays.sort(sorted, (Setting<?> o1, Setting<?> o2) -> {
            // Organize SponsorBlock settings last.
            // final boolean o1IsSb = o1.sharedPrefCategory == SPONSOR_BLOCK;
            // final boolean o2IsSb = o2.sharedPrefCategory == SPONSOR_BLOCK;
            // if (o1IsSb != o2IsSb) {
            //     return o1IsSb ? 1 : -1;
            // }

            return o1.key.compareTo(o2.key);
        });
        return sorted;
    }

    @NonNull
    public static String exportToJson(@Nullable Context alertDialogContext) {
        try {
            JSONObject json = new JSONObject();
            for (Setting<?> setting : sortSettingsByValueForExport()) {
                String importExportKey = setting.getImportExportKey();
                if (json.has(importExportKey)) {
                    throw new IllegalArgumentException("duplicate key found: " + importExportKey);
                }

                //noinspection ConstantValue
                final boolean exportDefaultValues = false; // Enable to see what all settings looks like in the UI.
                if (setting.includeWithImportExport && (!setting.isSetToDefault() | exportDefaultValues)) {
                    setting.writeToJSON(json, importExportKey);
                }
            }
            SponsorBlockSettings.exportCategoriesToFlatJson(alertDialogContext, json);

            if (json.length() == 0) {
                return "";
            }

            String export = json.toString(0);

            // Remove the outer JSON braces to make the output more compact,
            // and leave less chance of the user forgetting to copy it
            return export.substring(2, export.length() - 2);
        } catch (JSONException e) {
            Logger.printException(() -> "Export failure", e); // should never happen
            return "";
        }
    }

    /**
     * @return if any settings that require a reboot were changed.
     */
    public static boolean importFromJSON(@NonNull String settingsJsonString) {
        try {
            if (!settingsJsonString.matches("[\\s\\S]*\\{")) {
                settingsJsonString = '{' + settingsJsonString + '}'; // Restore outer JSON braces
            }
            JSONObject json = new JSONObject(settingsJsonString);

            boolean rebootSettingChanged = false;
            int numberOfSettingsImported = 0;
            for (Setting setting : SETTINGS) {
                String key = setting.getImportExportKey();
                if (json.has(key)) {
                    Object value = setting.readFromJSON(json, key);
                    if (!setting.get().equals(value)) {
                        rebootSettingChanged |= setting.rebootApp;
                        setting.save(value);
                    }
                    numberOfSettingsImported++;
                } else if (setting.includeWithImportExport && !setting.isSetToDefault()) {
                    Logger.printDebug(() -> "Resetting to default: " + setting);
                    rebootSettingChanged |= setting.rebootApp;
                    setting.resetToDefault();
                }
            }
            numberOfSettingsImported += SponsorBlockSettings.importCategoriesFromFlatJson(json);

            Utils.showToastLong(numberOfSettingsImported == 0
                    ? str("revanced_settings_import_reset")
                    : str("revanced_settings_import_success", numberOfSettingsImported));

            return rebootSettingChanged;
        } catch (JSONException | IllegalArgumentException ex) {
            Utils.showToastLong(str("revanced_settings_import_failure_parse", ex.getMessage()));
            Logger.printInfo(() -> "", ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Import failure: " + ex.getMessage(), ex); // should never happen
        }
        return false;
    }

    // End import / export

}
