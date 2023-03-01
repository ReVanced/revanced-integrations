package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SharedPrefHelper {
    public static void saveString(SharedPrefCategory prefName, String key, String value) {
        getPreferences(prefName).edit().putString(key, value).apply();
    }

    public static void saveBoolean(SharedPrefCategory prefName, String key, boolean value) {
        getPreferences(prefName).edit().putBoolean(key, value).apply();
    }

    public static void saveFloat(SharedPrefCategory prefName, String key, float value) {
        getPreferences(prefName).edit().putFloat(key, value).apply();
    }

    public static void saveInt(SharedPrefCategory prefName, String key, int value) {
        getPreferences(prefName).edit().putInt(key, value).apply();
    }

    public static void saveLong(SharedPrefCategory prefName, String key, long value) {
        getPreferences(prefName).edit().putLong(key, value).apply();
    }

    public static String getString(SharedPrefCategory prefName, String key, String _default) {
        return getPreferences(prefName).getString(key, _default);
    }

    public static boolean getBoolean(SharedPrefCategory prefName, String key, boolean _default) {
        return getPreferences(prefName).getBoolean(key, _default);
    }

    // region Hack, unknown why required

    public static Long getLong(SharedPrefCategory prefName, String key, Long _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Long.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getLong(key, _default);
        }
    }

    public static Float getFloat(SharedPrefCategory prefName, String key, Float _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Float.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getFloat(key, _default);
        }
    }

    public static Integer getInt(SharedPrefCategory prefName, String key, Integer _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Integer.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getInt(key, _default);
        }
    }

    // endregion

    public static SharedPreferences getPreferences(SharedPrefCategory name) {
        return Objects.requireNonNull(ReVancedUtils.getContext()).getSharedPreferences(name.prefName, Context.MODE_PRIVATE);
    }

}
