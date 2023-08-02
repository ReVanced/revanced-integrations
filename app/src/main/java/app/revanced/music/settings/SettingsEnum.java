package app.revanced.music.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.music.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.music.settings.SettingsEnum.ReturnType.FLOAT;
import static app.revanced.music.settings.SettingsEnum.ReturnType.INTEGER;
import static app.revanced.music.settings.SettingsEnum.ReturnType.LONG;
import static app.revanced.music.settings.SettingsEnum.ReturnType.STRING;
import static app.revanced.music.utils.StringRef.str;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.music.utils.ReVancedUtils;
import app.revanced.music.utils.StringRef;
import app.revanced.music.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public enum SettingsEnum {
    // Layout
    PERMANENT_REPEAT("revanced_permanent_repeat", BOOLEAN, FALSE),
    PERMANENT_SHUFFLE("revanced_permanent_shuffle", BOOLEAN, FALSE),

    // Debugging
    DEBUG("revanced_debug", BOOLEAN, FALSE),
    DEBUG_STACKTRACE("revanced_debug_stacktrace", BOOLEAN, FALSE, parents(DEBUG)),
    DEBUG_TOAST_ON_ERROR("revanced_debug_toast_on_error", BOOLEAN, TRUE, "revanced_debug_toast_on_error_user_dialog_message");

    private static SettingsEnum[] parents(SettingsEnum... parents) {
        return parents;
    }

    @NonNull
    public final String path;
    @NonNull
    public final Object defaultValue;
    @NonNull
    public final SharedPrefCategory sharedPref;
    @NonNull
    public final ReturnType returnType;
    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;
    /**
     * Set of boolean parent settings.
     * If any of the parents are enabled, then this setting is available to configure.
     *
     * For example: {@link #DEBUG_STACKTRACE} is non-functional and cannot be configured,
     * unless it's parent {@link #DEBUG} is enabled.
     *
     * Declaration is not needed for items that do not appear in the ReVanced Settings UI.
     */
    @Nullable
    private final SettingsEnum[] parents;

    /**
     * Confirmation message to display, if the user tries to change the setting from the default value.
     * Can only be used for {@link ReturnType#BOOLEAN} setting types.
     */
    @Nullable
    public final StringRef userDialogMessage;

    // Must be volatile, as some settings are read/write from different threads.
    // Of note, the object value is persistently stored using SharedPreferences (which is thread safe).
    @NonNull
    private volatile Object value;

    SettingsEnum(String path, ReturnType returnType, Object defaultValue) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, false, null, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, rebootApp, null, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 String userDialogMessage) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, false, userDialogMessage, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, false, null, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, String userDialogMessage) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, rebootApp, userDialogMessage, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, rebootApp, null, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, String userDialogMessage, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE_MUSIC, rebootApp, userDialogMessage, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName) {
        this(path, returnType, defaultValue, prefName, false, null, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 boolean rebootApp) {
        this(path, returnType, defaultValue, prefName, rebootApp, null, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 String userDialogMessage) {
        this(path, returnType, defaultValue, prefName, false, userDialogMessage, null);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 SettingsEnum[] parents) {
        this(path, returnType, defaultValue, prefName, false, null, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 boolean rebootApp, @Nullable String userDialogMessage, @Nullable SettingsEnum[] parents) {
        this.path = Objects.requireNonNull(path);
        this.returnType = Objects.requireNonNull(returnType);
        this.value = this.defaultValue = Objects.requireNonNull(defaultValue);
        this.sharedPref = Objects.requireNonNull(prefName);
        this.rebootApp = rebootApp;

        if (userDialogMessage == null) {
            this.userDialogMessage = null;
        } else {
            if (returnType != ReturnType.BOOLEAN) {
                throw new IllegalArgumentException("must be Boolean type: " + path);
            }
            this.userDialogMessage = new StringRef(userDialogMessage);
        }

        this.parents = parents;
        if (parents != null) {
            for (SettingsEnum parent : parents) {
                if (parent.returnType != ReturnType.BOOLEAN) {
                    throw new IllegalArgumentException("parent must be Boolean type: " + parent);
                }
            }
        }
    }

    private static final Map<String, SettingsEnum> pathToSetting = new HashMap<>(2* values().length);

    static {
        loadAllSettings();

        for (SettingsEnum setting : values()) {
            pathToSetting.put(setting.path, setting);
        }
    }

    @Nullable
    public static SettingsEnum settingFromPath(@NonNull String str) {
        return pathToSetting.get(str);
    }

    private static void loadAllSettings() {
        for (SettingsEnum setting : values()) {
            setting.load();
        }
    }

    /**
     * Migrate a setting value if the path is renamed but otherwise the old and new settings are identical.
     */
    private static void migrateOldSettingToNew(SettingsEnum oldSetting, SettingsEnum newSetting) {
        if (!oldSetting.isSetToDefault()) {
            LogHelper.printInfo(() -> "Migrating old setting of '" + oldSetting.value
                    + "' from: " + oldSetting + " into replacement setting: " + newSetting);
            newSetting.saveValue(oldSetting.value);
            oldSetting.saveValue(oldSetting.defaultValue); // reset old value
        }
    }

    private void load() {
        switch (returnType) {
            case BOOLEAN:
                value = sharedPref.getBoolean(path, (boolean) defaultValue);
                break;
            case INTEGER:
                value = sharedPref.getIntegerString(path, (Integer) defaultValue);
                break;
            case LONG:
                value = sharedPref.getLongString(path, (Long) defaultValue);
                break;
            case FLOAT:
                value = sharedPref.getFloatString(path, (Float) defaultValue);
                break;
            case STRING:
                value = sharedPref.getString(path, (String) defaultValue);
                break;
            default:
                throw new IllegalStateException(name());
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
    public static void setValue(@NonNull SettingsEnum setting, @NonNull String newValue) {
        Objects.requireNonNull(newValue);
        switch (setting.returnType) {
            case BOOLEAN:
                setting.value = Boolean.valueOf(newValue);
                break;
            case INTEGER:
                setting.value = Integer.valueOf(newValue);
                break;
            case LONG:
                setting.value = Long.valueOf(newValue);
                break;
            case FLOAT:
                setting.value = Float.valueOf(newValue);
                break;
            case STRING:
                setting.value = newValue;
                break;
            default:
                throw new IllegalStateException(setting.name());
        }
    }

    /**
     * This method is only to be used by the Settings preference code.
     */
    public static void setValue(@NonNull SettingsEnum setting, @NonNull Boolean newValue) {
        setting.returnType.validate(newValue);
        setting.value = newValue;
    }

    /**
     * Sets the value, and persistently saves it.
     */
    public void saveValue(@NonNull Object newValue) {
        returnType.validate(newValue);
        value = newValue; // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        switch (returnType) {
            case BOOLEAN:
                sharedPref.saveBoolean(path, (boolean) newValue);
                break;
            case INTEGER:
                sharedPref.saveIntegerString(path, (Integer) newValue);
                break;
            case LONG:
                sharedPref.saveLongString(path, (Long) newValue);
                break;
            case FLOAT:
                sharedPref.saveFloatString(path, (Float) newValue);
                break;
            case STRING:
                sharedPref.saveString(path, (String) newValue);
                break;
            default:
                throw new IllegalStateException(name());
        }
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
        for (SettingsEnum parent : parents) {
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
     * @return the value of this setting as as generic object type.
     */
    @NonNull
    public Object getObjectValue() {
        return value;
    }

    /**
     * This could be yet another field,
     * for now use a simple switch statement since this method is not used outside this class.
     */
    private boolean includeWithImportExport() {
        return true;
    }

    // Begin import / export

    /**
     * If a setting path has this prefix, then remove it before importing/exporting.
     */
    private static final String OPTIONAL_REVANCED_SETTINGS_PREFIX = "revanced_";

    /**
     * The path, minus any 'revanced' prefix to keep json concise.
     */
    private String getImportExportKey() {
        if (path.startsWith(OPTIONAL_REVANCED_SETTINGS_PREFIX)) {
            return path.substring(OPTIONAL_REVANCED_SETTINGS_PREFIX.length());
        }
        return path;
    }

    private static SettingsEnum[] valuesSortedForExport() {
        SettingsEnum[] sorted = values();
        Arrays.sort(sorted, (SettingsEnum o1, SettingsEnum o2) -> {
            return o1.path.compareTo(o2.path);
        });
        return sorted;
    }

    @NonNull
    public static String exportJSON(@Nullable Context alertDialogContext) {
        try {
            JSONObject json = new JSONObject();
            for (SettingsEnum setting : valuesSortedForExport()) {
                String importExportKey = setting.getImportExportKey();
                if (json.has(importExportKey)) {
                    throw new IllegalArgumentException("duplicate key found: " + importExportKey);
                }
                final boolean exportDefaultValues = false; // Enable to see what all settings looks like in the UI.
                if (setting.includeWithImportExport() && (!setting.isSetToDefault() | exportDefaultValues)) {
                    json.put(importExportKey, setting.getObjectValue());
                }
            }

            if (json.length() == 0) {
                return "";
            }
            String export = json.toString(0);
            // Remove the outer JSON braces to make the output more compact,
            // and leave less chance of the user forgetting to copy it
            return export.substring(2, export.length() - 2);
        } catch (JSONException e) {
            LogHelper.printException(() -> "Export failure", e); // should never happen
            return "";
        }
    }

    /**
     * @return if any settings that require a reboot were changed.
     */
    public static boolean importJSON(@NonNull String settingsJsonString) {
        try {
            if (!settingsJsonString.matches("[\\s\\S]*\\{")) {
                settingsJsonString = '{' + settingsJsonString + '}'; // Restore outer JSON braces
            }
            JSONObject json = new JSONObject(settingsJsonString);

            boolean rebootSettingChanged = false;
            int numberOfSettingsImported = 0;
            for (SettingsEnum setting : values()) {
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
                } else if (setting.includeWithImportExport() && !setting.isSetToDefault()) {
                    LogHelper.printDebug(() -> "Resetting to default: " + setting);
                    rebootSettingChanged |= setting.rebootApp;
                    setting.saveValue(setting.defaultValue);
                }
            }

            ReVancedUtils.showToastLong(numberOfSettingsImported == 0
                    ? str("revanced_settings_import_reset")
                    : str("revanced_settings_import_success", numberOfSettingsImported));

            return rebootSettingChanged;
        } catch (JSONException | IllegalArgumentException ex) {
            ReVancedUtils.showToastLong(str("revanced_settings_import_failure_parse", ex.getMessage()));
            LogHelper.printInfo(() -> "", ex);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Import failure: " + ex.getMessage(), ex); // should never happen
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
