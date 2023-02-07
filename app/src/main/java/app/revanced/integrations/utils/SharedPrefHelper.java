package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SharedPrefHelper {
    public static void saveString(SharedPrefNames prefName, String key, String value) {
        SharedPreferences.Editor edit = getPreferences(prefName).edit();
        edit.putString(key, value).apply();
        edit.commit();
    }

    public static void saveBoolean(SharedPrefNames prefName, String key, boolean value) {
        SharedPreferences.Editor edit = getPreferences(prefName).edit();
        edit.putBoolean(key, value).apply();
        edit.commit();
    }

    public static void saveFloat(SharedPrefNames prefName, String key, float value) {
        SharedPreferences.Editor edit = getPreferences(prefName).edit();
        edit.putFloat(key, value).apply();
        edit.commit();
    }

    public static void saveInt(SharedPrefNames prefName, String key, int value) {
        SharedPreferences.Editor edit = getPreferences(prefName).edit();
        edit.putInt(key, value).apply();
        edit.commit();
    }

    public static void saveLong(SharedPrefNames prefName, String key, long value) {
        SharedPreferences.Editor edit = getPreferences(prefName).edit();
        edit.putLong(key, value).apply();
        edit.commit();
    }

    public static String getString(SharedPrefNames prefName, String key, String _default) {
        try {
            return getPreferences(prefName).getString(key, _default);
        } catch (ClassCastException ex) {
            LogHelper.printException(() -> "Could not load: " + prefName + " (wrong class type stored)");
            saveString(prefName, key, _default); // overwrite the bad preference
            return _default;
        }
    }

    public static boolean getBoolean(SharedPrefNames prefName, String key, boolean _default) {
        try {
            return getPreferences(prefName).getBoolean(key, _default);
        } catch (ClassCastException ex) {
            LogHelper.printException(() -> "Could not load: " + prefName + " (wrong class type stored)");
            saveBoolean(prefName, key, _default); // overwrite the bad preference
            return _default;
        }
    }

    public static long getLong(SharedPrefNames prefName, String key, long _default) {
        try {
            return getPreferences(prefName).getLong(key, _default);
        } catch (ClassCastException ex) {
            LogHelper.printException(() -> "Could not load: " + prefName + " (wrong class type stored)");
            saveLong(prefName, key, _default); // overwrite the bad preference
            return _default;
        }
    }

    public static float getFloat(SharedPrefNames prefName, String key, float _default) {
        try {
            return getPreferences(prefName).getFloat(key, _default);
        } catch (ClassCastException ex) {
            LogHelper.printException(() -> "Could not load: " + prefName + " (wrong class type stored)");
            saveFloat(prefName, key, _default); // overwrite the bad preference
            return _default;
        }
    }

    public static int getInt(SharedPrefNames prefName, String key, int _default) {
        try {
            return getPreferences(prefName).getInt(key, _default);
        } catch (ClassCastException ex) {
            LogHelper.printException(() -> "Could not load: " + prefName + " (wrong class type stored)");
            saveInt(prefName, key, _default); // overwrite the bad preference
            return _default;
        }
    }

    public static SharedPreferences getPreferences(SharedPrefNames name) {
        return Objects.requireNonNull(ReVancedUtils.getContext()).getSharedPreferences(name.getName(), Context.MODE_PRIVATE);
    }

    public enum SharedPrefNames {
        YOUTUBE("youtube"),
        RYD("ryd"),
        SPONSOR_BLOCK("sponsor-block"),
        REVANCED_PREFS("revanced_prefs");

        private final String name;

        SharedPrefNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
