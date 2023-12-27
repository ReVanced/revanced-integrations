package app.revanced.integrations.shared.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import app.revanced.integrations.youtube.settings.SharedPrefCategory;

public class StringSetting extends Setting<String> {

    public StringSetting(String key, String defaultValue) {
        super(key, defaultValue);
    }
    public StringSetting(String key, String defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    public StringSetting(String key, String defaultValue, String userDialogMessage) {
        super(key, defaultValue, userDialogMessage);
    }
    public StringSetting(String key, String defaultValue, BooleanSetting[] parents) {
        super(key, defaultValue, parents);
    }
    public StringSetting(String key, String defaultValue, boolean rebootApp, String userDialogMessage) {
        super(key, defaultValue, rebootApp, userDialogMessage);
    }
    public StringSetting(String key, String defaultValue, boolean rebootApp, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, parents);
    }
    public StringSetting(String key, String defaultValue, boolean rebootApp, String userDialogMessage, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, userDialogMessage, parents);
    }
    public StringSetting(String key, String defaultValue, SharedPrefCategory prefName) {
        super(key, defaultValue, prefName);
    }
    public StringSetting(String key, String defaultValue, SharedPrefCategory prefName, boolean rebootApp) {
        super(key, defaultValue, prefName, rebootApp);
    }
    public StringSetting(@NonNull String key, @NonNull String defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, rebootApp, includeWithImportExport);
    }
    public StringSetting(String key, String defaultValue, SharedPrefCategory prefName, String userDialogMessage) {
        super(key, defaultValue, prefName, userDialogMessage);
    }
    public StringSetting(String key, String defaultValue, SharedPrefCategory prefName, BooleanSetting[] parents) {
        super(key, defaultValue, prefName, parents);
    }
    public StringSetting(@NonNull String key, @NonNull String defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport);
    }
    public StringSetting(@NonNull String key, @NonNull String defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport, @Nullable String userDialogMessage, @Nullable BooleanSetting[] parents) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport, userDialogMessage, parents);
    }

    @Override
    protected void load() {
        value = sharedPrefCategory.getString(key, defaultValue);
    }

    @Override
    protected String readFromJSON(JSONObject json, String importExportKey) throws JSONException {
        return json.getString(importExportKey);
    }

    @Override
    protected void setValueFromString(@NonNull String newValue) {
        value = Objects.requireNonNull(newValue);
    }

    @Override
    public void save(@NonNull String newValue) {
        // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        value = Objects.requireNonNull(newValue);
        sharedPrefCategory.saveString(key, newValue);
    }

    @NonNull
    @Override
    public String get() {
        return value;
    }
}
