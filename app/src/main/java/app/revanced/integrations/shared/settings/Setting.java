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


// TODO: Add generic type to Setting in order to add a Setting#get() method that returns the correct type.

/**
 * A setting backed by a shared preference.
 */
public class Setting {
    /**
     * All settings that were instantiated.
     * When a new setting is created, it is automatically added to this list.
     */
    private static final List<Setting> SETTINGS = new LinkedList<>();

    /**
     * Map of setting path to setting object.
     */
    private static final Map<String, Setting> PATH_TO_SETTINGS = new HashMap<>();

    /**
     * The key used to store the value in the shared preferences.
     */
    @NonNull
    public final String key;

    /**
     * The default value of the setting.
     */
    @NonNull
    public final Object defaultValue;

    /**
     * The category of the shared preferences to store the value in.
     */
    @NonNull
    public final SharedPrefCategory sharedPrefCategory;

    /**
     * The type of the setting.
     */
    @NonNull
    public final ReturnType returnType;

    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;

    /**
     * Set of boolean parent settings.
     * If any of the parents are enabled, then this setting is available to configure.
     * <p>
     * Declaration is not needed for items that do not appear in the ReVanced Settings UI.
     */
    @Nullable
    private final Setting[] parents;

    /**
     * Confirmation message to display, if the user tries to change the setting from the default value.
     * Can only be used for {@link ReturnType#BOOLEAN} setting types.
     */
    @Nullable
    public final StringRef userDialogMessage;

    // Must be volatile, as some settings are read/write from different threads.
    // Of note, the object value is persistently stored using SharedPreferences (which is thread safe).
    /**
     * The value of the setting.
     */
    @NonNull
    private volatile Object value;

    // TODO: Set this via a constructor parameter.
    /**
     * If this setting should be included when importing/exporting settings.
     */
    private final boolean includeWithImportExport = true;


    public Setting(String key, ReturnType returnType, Object defaultValue) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, null, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   boolean rebootApp) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   String userDialogMessage) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, userDialogMessage, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   Setting[] parents) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, null, parents);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   boolean rebootApp, String userDialogMessage) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, userDialogMessage, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   boolean rebootApp, Setting[] parents) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null, parents);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue,
                   boolean rebootApp, String userDialogMessage, Setting[] parents) {
        this(key, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, userDialogMessage, parents);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName) {
        this(key, returnType, defaultValue, prefName, false, null, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                   boolean rebootApp) {
        this(key, returnType, defaultValue, prefName, rebootApp, null, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                   String userDialogMessage) {
        this(key, returnType, defaultValue, prefName, false, userDialogMessage, null);
    }

    public Setting(String key, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                   Setting[] parents) {
        this(key, returnType, defaultValue, prefName, false, null, parents);
    }

    /**
     * A setting backed by a shared preference.
     * @param key The key used to store the value in the shared preferences.
     * @param returnType The type of the setting.
     * @param defaultValue The default value of the setting.
     * @param prefName The category of the shared preferences to store the value in.
     * @param rebootApp If the app should be rebooted, if this setting is changed.
     * @param userDialogMessage Confirmation message to display, if the user tries to change the setting from the default value.
     * @param parents Set of boolean parent settings that must be enabled, for this setting to be available to configure.
     */
    public Setting(@NonNull String key,
                   @NonNull ReturnType returnType,
                   @NonNull Object defaultValue,
                   @NonNull SharedPrefCategory prefName,
                   boolean rebootApp,
                   @Nullable String userDialogMessage,
                   @Nullable Setting[] parents
    ) {
        this.key = Objects.requireNonNull(key);
        this.returnType = Objects.requireNonNull(returnType);
        this.value = this.defaultValue = Objects.requireNonNull(defaultValue);
        this.sharedPrefCategory = Objects.requireNonNull(prefName);
        this.rebootApp = rebootApp;

        if (userDialogMessage == null) {
            this.userDialogMessage = null;
        } else {
            if (returnType != ReturnType.BOOLEAN) {
                throw new IllegalArgumentException("Must be Boolean type: " + key);
            }
            this.userDialogMessage = new StringRef(userDialogMessage);
        }

        this.parents = parents;
        if (parents != null) {
            for (Setting parent : parents) {
                if (parent.returnType != ReturnType.BOOLEAN) {
                    throw new IllegalArgumentException("Parent must be Boolean type: " + parent);
                }
            }
        }

        // Set the value of the setting saved in shared preferences.
        switch (returnType) {
            case BOOLEAN:
                value = sharedPrefCategory.getBoolean(key, (boolean) defaultValue);
                break;
            case INTEGER:
                value = sharedPrefCategory.getIntegerString(key, (Integer) defaultValue);
                break;
            case LONG:
                value = sharedPrefCategory.getLongString(key, (Long) defaultValue);
                break;
            case FLOAT:
                value = sharedPrefCategory.getFloatString(key, (Float) defaultValue);
                break;
            case STRING:
                value = sharedPrefCategory.getString(key, (String) defaultValue);
                break;
            default:
                throw new IllegalStateException(toString());
        }

        SETTINGS.add(this);
        PATH_TO_SETTINGS.put(key, this);
    }

    public static Setting[] getParents(Setting... parents) {
        return parents;
    }

    @Nullable
    public static Setting getSettingFromPath(@NonNull String str) {
        return PATH_TO_SETTINGS.get(str);
    }

    /**
     * Migrate a setting value if the path is renamed but otherwise the old and new settings are identical.
     */
    public static void migrateOldSettingToNew(Setting oldSetting, Setting newSetting) {
        if (!oldSetting.isSetToDefault()) {
            Logger.printInfo(() -> "Migrating old setting of '" + oldSetting.value
                    + "' from: " + oldSetting + " into replacement setting: " + newSetting);
            newSetting.saveValue(oldSetting.value);
            oldSetting.resetToDefault();
        }
    }

    /**
     * Sets, but does _not_ persistently save the value.
     * <p>
     * This intentionally is a static method, to deter accidental usage
     * when {@link #saveValue(Object)} was intended.
     * <p>
     * This method is only to be used by the Settings preference code.
     */
    public void setValue(@NonNull String newValue) {
        Objects.requireNonNull(newValue);
        switch (returnType) {
            case BOOLEAN:
                value = Boolean.valueOf(newValue);
                break;
            case INTEGER:
                value = Integer.valueOf(newValue);
                break;
            case LONG:
                value = Long.valueOf(newValue);
                break;
            case FLOAT:
                value = Float.valueOf(newValue);
                break;
            case STRING:
                value = newValue;
                break;
            default:
                throw new IllegalStateException(toString());
        }
    }

    /**
     * This method is only to be used by the Settings preference code.
     */
    public void setValue(@NonNull Boolean newValue) {
        returnType.validate(newValue);
        value = newValue;
    }

    /**
     * Sets the value, and persistently saves it.
     */
    public void saveValue(@NonNull Object newValue) {
        returnType.validate(newValue);
        value = newValue; // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        switch (returnType) {
            case BOOLEAN:
                sharedPrefCategory.saveBoolean(key, (boolean) newValue);
                break;
            case INTEGER:
                sharedPrefCategory.saveIntegerString(key, (Integer) newValue);
                break;
            case LONG:
                sharedPrefCategory.saveLongString(key, (Long) newValue);
                break;
            case FLOAT:
                sharedPrefCategory.saveFloatString(key, (Float) newValue);
                break;
            case STRING:
                sharedPrefCategory.saveString(key, (String) newValue);
                break;
            default:
                throw new IllegalStateException(toString());
        }
    }

    /**
     * Identical to calling {@link #saveValue(Object)} using {@link #defaultValue}.
     */
    public void resetToDefault() {
        saveValue(defaultValue);
    }

    /**
     * @return if this setting can be configured and used.
     * <p>
     * Not to be confused with {@link #getBoolean()}
     */
    public boolean isAvailable() {
        if (parents == null) {
            return true;
        }
        for (Setting parent : parents) {
            if (parent.getBoolean()) return true;
        }
        return false;
    }

    /**
     * @return if the currently set value is the same as {@link #defaultValue}
     */
    public boolean isSetToDefault() {
        return value.equals(defaultValue);
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public int getInt() {
        return (Integer) value;
    }

    public long getLong() {
        return (Long) value;
    }

    public float getFloat() {
        return (Float) value;
    }

    @NonNull
    public String getString() {
        return (String) value;
    }

    /**
     * @return the value of this setting as generic object type.
     */
    @NonNull
    public Object getObjectValue() {
        return value;
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
            ((SwitchPreference) preference).setChecked(getBoolean());
        } else if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).setText(getObjectValue().toString());
        } else if (preference instanceof ListPreference) {
            setListPreference((ListPreference) preference, this);
        }
    }

    /** @noinspection deprecation*/
    public static void setListPreference(ListPreference listPreference, Setting setting) {
        String objectStringValue = setting.getObjectValue().toString();
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
        return key + "=" + getObjectValue();
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

    private static Setting[] sortSettingsByValueForExport() {
        Setting[] sorted = SETTINGS.toArray(new Setting[0]);

        // TODO: Figure out an object oriented way to do this.
        Arrays.sort(sorted, (Setting o1, Setting o2) -> {
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
            for (Setting setting : sortSettingsByValueForExport()) {
                String importExportKey = setting.getImportExportKey();

                if (json.has(importExportKey)) {
                    throw new IllegalArgumentException("duplicate key found: " + importExportKey);
                }

                final boolean exportDefaultValues = false; // Enable to see what all settings looks like in the UI.

                //noinspection ConstantValue
                if (setting.includeWithImportExport && (!setting.isSetToDefault() | exportDefaultValues)) {
                    json.put(importExportKey, setting.getObjectValue());
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
                    Object value;
                    switch (setting.returnType) {
                        case BOOLEAN:
                            value = json.getBoolean(key);
                            break;
                        case INTEGER:
                            value = json.getInt(key);
                            break;
                        case LONG:
                            value = json.getLong(key);
                            break;
                        case FLOAT:
                            value = (float) json.getDouble(key);
                            break;
                        case STRING:
                            value = json.getString(key);
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    if (!setting.getObjectValue().equals(value)) {
                        rebootSettingChanged |= setting.rebootApp;
                        setting.saveValue(value);
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

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        LONG,
        FLOAT,
        STRING;

        public void validate(@Nullable Object obj) throws IllegalArgumentException {
            if (!matches(obj)) {
                throw new IllegalArgumentException("'" + obj + "' does not match:" + this);
            }
        }

        public boolean matches(@Nullable Object obj) {
            switch (this) {
                case BOOLEAN:
                    return obj instanceof Boolean;
                case INTEGER:
                    return obj instanceof Integer;
                case LONG:
                    return obj instanceof Long;
                case FLOAT:
                    return obj instanceof Float;
                case STRING:
                    return obj instanceof String;
            }
            return false;
        }
    }
}
