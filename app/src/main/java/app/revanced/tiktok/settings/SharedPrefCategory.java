package app.revanced.tiktok.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.tiktok.utils.ReVancedUtils;

public enum SharedPrefCategory {
    TIKTOK_PREFS("tiktok_revanced");

    @NonNull
    public final String prefName;
    @NonNull
    public final SharedPreferences preferences;

    SharedPrefCategory(@NonNull String prefName) {
        this.prefName = prefName;
        preferences = Objects.requireNonNull(ReVancedUtils.getAppContext()).getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public String toString() {
        return prefName;
    }

    private void saveObjectAsString(@NonNull String key, @Nullable Object value) {
        preferences.edit().putString(key, (value == null ? null : value.toString())).apply();
    }

    public void saveBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void saveString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public void saveFloatString(String key, Float value) {
        saveObjectAsString(key, value);
    }

    public boolean getBoolean(String key, boolean _default) {
        return preferences.getBoolean(key, _default);
    }

    public Integer getInt(String key, Integer _default) {
        try {
            var value = preferences.getString(key, null);
            if (value != null)
                return Integer.valueOf(value);
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getInt(key, _default);
        }
    }

    public Long getLong(String key, Long _default) {
        try {
            var value = preferences.getString(key, null);
            if (value != null)
                return Long.valueOf(value);
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getLong(key, _default);
        }
    }

    public Float getFloat(String key, Float _default) {
        try {
            var value = preferences.getString(key, null);
            if (value != null)
                return Float.valueOf(value);
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getFloat(key, _default);
        }
    }

    public String getString(String key, String _default) {
        return preferences.getString(key, _default);
    }

}
