package app.revanced.integrations.shared.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FloatSetting extends Setting<Float> {

    public FloatSetting(String key, Float defaultValue) {
        super(key, defaultValue);
    }
    public FloatSetting(String key, Float defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    public FloatSetting(String key, Float defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, rebootApp, includeWithImportExport);
    }
    public FloatSetting(String key, Float defaultValue, String userDialogMessage) {
        super(key, defaultValue, userDialogMessage);
    }
    public FloatSetting(String key, Float defaultValue, BooleanSetting[] parents) {
        super(key, defaultValue, parents);
    }
    public FloatSetting(String key, Float defaultValue, boolean rebootApp, String userDialogMessage) {
        super(key, defaultValue, rebootApp, userDialogMessage);
    }
    public FloatSetting(String key, Float defaultValue, boolean rebootApp, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, parents);
    }
    public FloatSetting(String key, Float defaultValue, boolean rebootApp, String userDialogMessage, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, userDialogMessage, parents);
    }
    public FloatSetting(String key, Float defaultValue, SharedPrefCategory prefName) {
        super(key, defaultValue, prefName);
    }
    public FloatSetting(String key, Float defaultValue, SharedPrefCategory prefName, boolean rebootApp) {
        super(key, defaultValue, prefName, rebootApp);
    }
    public FloatSetting(String key, Float defaultValue, SharedPrefCategory prefName, String userDialogMessage) {
        super(key, defaultValue, prefName, userDialogMessage);
    }
    public FloatSetting(String key, Float defaultValue, SharedPrefCategory prefName, BooleanSetting[] parents) {
        super(key, defaultValue, prefName, parents);
    }
    public FloatSetting(String key, Float defaultValue, SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport);
    }
    public FloatSetting(@NonNull String key, @NonNull Float defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport, @Nullable String userDialogMessage, @Nullable BooleanSetting[] parents) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport, userDialogMessage, parents);
    }

    @Override
    protected void load() {
        value = sharedPrefCategory.getFloatString(key, defaultValue);
    }

    @Override
    protected Float readFromJSON(JSONObject json, String importExportKey) throws JSONException {
        return (float) json.getDouble(importExportKey);
    }

    @Override
    protected void setValueFromString(@NonNull String newValue) {
        value = Float.valueOf(Objects.requireNonNull(newValue));
    }

    @Override
    public void save(@NonNull Float newValue) {
        // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        value = Objects.requireNonNull(newValue);
        sharedPrefCategory.saveFloatString(key, newValue);
    }

    @NonNull
    @Override
    public Float get() {
        return value;
    }
}
