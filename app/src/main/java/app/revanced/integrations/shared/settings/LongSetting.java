package app.revanced.integrations.shared.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import app.revanced.integrations.youtube.settings.SharedPrefCategory;

public class LongSetting extends Setting<Long> {

    public LongSetting(String key, Long defaultValue) {
        super(key, defaultValue);
    }
    public LongSetting(String key, Long defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    public LongSetting(@NonNull String key, @NonNull Long defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, rebootApp, includeWithImportExport);
    }
    public LongSetting(String key, Long defaultValue, String userDialogMessage) {
        super(key, defaultValue, userDialogMessage);
    }
    public LongSetting(String key, Long defaultValue, BooleanSetting[] parents) {
        super(key, defaultValue, parents);
    }
    public LongSetting(String key, Long defaultValue, boolean rebootApp, String userDialogMessage) {
        super(key, defaultValue, rebootApp, userDialogMessage);
    }
    public LongSetting(String key, Long defaultValue, boolean rebootApp, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, parents);
    }
    public LongSetting(String key, Long defaultValue, boolean rebootApp, String userDialogMessage, BooleanSetting[] parents) {
        super(key, defaultValue, rebootApp, userDialogMessage, parents);
    }
    public LongSetting(String key, Long defaultValue, SharedPrefCategory prefName) {
        super(key, defaultValue, prefName);
    }
    public LongSetting(String key, Long defaultValue, SharedPrefCategory prefName, boolean rebootApp) {
        super(key, defaultValue, prefName, rebootApp);
    }
    public LongSetting(String key, Long defaultValue, SharedPrefCategory prefName, String userDialogMessage) {
        super(key, defaultValue, prefName, userDialogMessage);
    }
    public LongSetting(String key, Long defaultValue, SharedPrefCategory prefName, BooleanSetting[] parents) {
        super(key, defaultValue, prefName, parents);
    }
    public LongSetting(@NonNull String key, @NonNull Long defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport);
    }
    public LongSetting(@NonNull String key, @NonNull Long defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp, boolean includeWithImportExport, @Nullable String userDialogMessage, @Nullable BooleanSetting[] parents) {
        super(key, defaultValue, prefName, rebootApp, includeWithImportExport, userDialogMessage, parents);
    }

    @Override
    protected void load() {
        value = sharedPrefCategory.getLongString(key, defaultValue);
    }

    @Override
    protected void setValueFromString(@NonNull String newValue) {
        value = Long.valueOf(Objects.requireNonNull(newValue));
    }

    @Override
    public void save(@NonNull Long newValue) {
        // Must set before saving to preferences (otherwise importing fails to update UI correctly).
        value = Objects.requireNonNull(newValue);
        sharedPrefCategory.saveLongString(key, newValue);
    }

    @NonNull
    @Override
    public Long get() {
        return value;
    }

    @Override
    protected Long getJsonValue(JSONObject json) throws JSONException {
        return json.getLong(key);
    }
}
