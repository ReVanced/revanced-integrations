package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SharedPrefHelper {
    public static void saveString(SharedPrefCategory prefName, String key, String value) {
        prefName.saveString(key, value);
    }

    public static void saveBoolean(SharedPrefCategory prefName, String key, boolean value) {
        prefName.saveBoolean(key, value);
    }

    public static void saveFloat(SharedPrefCategory prefName, String key, float value) {
        prefName.saveFloat(key, value);
    }

    public static void saveInt(SharedPrefCategory prefName, String key, int value) {
        prefName.saveInt(key, value);
    }

    public static void saveLong(SharedPrefCategory prefName, String key, long value) {
        prefName.saveLong(key, value);
    }

    public static String getString(SharedPrefCategory prefName, String key, String _default) {
        return prefName.getString(key, _default);
    }

    public static boolean getBoolean(SharedPrefCategory prefName, String key, boolean _default) {
        return prefName.getBoolean(key, _default);
    }

    public static Long getLong(SharedPrefCategory prefName, String key, Long _default) {
        return prefName.getLong(key, _default);
    }

    public static Float getFloat(SharedPrefCategory prefName, String key, Float _default) {
        return prefName.getFloat(key, _default);
    }

    public static Integer getInt(SharedPrefCategory prefName, String key, Integer _default) {
        return prefName.getInt(key, _default);
    }

    public static SharedPreferences getPreferences(SharedPrefCategory name) {
        return name.preferences;
    }

}
