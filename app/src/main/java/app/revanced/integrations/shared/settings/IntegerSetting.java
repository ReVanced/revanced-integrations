package app.revanced.integrations.shared.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class IntegerSetting extends Setting<Integer> {

    public IntegerSetting(String key, Integer defaultValue) {
        super(key, defaultValue);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, rebootApp, includeWithImportExport);
    }
    public IntegerSetting(String key, Integer defaultValue, String userDialogMessage) {
        super(key, defaultValue, userDialogMessage);
    }
    public IntegerSetting(String key, Integer defaultValue, BooleanSetting[] parents) {
        super(key, defaultValue, parents);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp, String userDialogMessage) {
        super(key, defaultValue, rebootApp, userDialogMessage);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, parents);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp, String userDialogMessage, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, userDialogMessage, parents);
    }
    public IntegerSetting(String key, Integer defaultValue, SharedPrefCategory prefName) {
        super(key, defaultValue, prefName);
    }
    public IntegerSetting(String key, Integer defaultValue, SharedPrefCategory prefName, boolean rebootApp) {
        super(key, defaultValue, prefName, rebootApp);
    }
    public IntegerSetting(String key, Integer defaultValue, SharedPrefCategory prefName, String userDialogMessage) {
        super(key, defaultValue, prefName, userDialogMessage);
    }
    public IntegerSetting(String key, Integer defaultValue, SharedPrefCategory prefName, BooleanSetting[] parents) {
        super(key, defaultValue, prefName, parents);
    }
    public IntegerSetting(String key, Integer defaultValue, SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport);
    }
    public IntegerSetting(@NonNull String key, @NonNull Integer defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport, @Nullable String userDialogMessage, @Nullable BooleanSetting[] parents) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport, userDialogMessage, parents);
    }

    @Override
    protected void load() {
        value = sharedPrefCategory.getIntegerString(key, defaultValue);
    }

    @Override
    protected Integer readFromJSON(JSONObject json, String importExportKey) throws JSONException {
        return json.getInt(importExportKey);
    }

    @Override
    protected void setValueFromString(@NonNull String newValue) {
        value = Integer.valueOf(Objects.requireNonNull(newValue));
    }

    @Override
    public void save(@NonNull Integer newValue) {
        // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        value = Objects.requireNonNull(newValue);
        sharedPrefCategory.saveIntegerString(key, newValue);
    }

    @NonNull
    @Override
    public Integer get() {
        return value;
    }
}
