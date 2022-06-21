package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {
    public static void saveString(Context context, SharedPrefNames prefName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static void saveBoolean(Context context, SharedPrefNames prefName, String key, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static void saveInt(Context context, SharedPrefNames prefName, String key, Integer value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static String getString(Context context, SharedPrefNames prefName, String key) {
        return getString(context, prefName, key, null);
    }

    public static String getString(Context context, SharedPrefNames prefName, String key, String _default) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        return (sharedPreferences.getString(key, _default));
    }

    public static Boolean getBoolean(Context context, SharedPrefNames prefName, String key) {
        return getBoolean(context, prefName, key, false);
    }

    public static Boolean getBoolean(Context context, SharedPrefNames prefName, String key, Boolean _default) {
        if (context == null) return false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        return (sharedPreferences.getBoolean(key, _default));
    }

    public static Integer getInt(Context context, SharedPrefNames prefName, String key) {
        return getInt(context, prefName, key, -1);
    }

    public static Integer getInt(Context context, SharedPrefNames prefName, String key, Integer _default) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName.getName(), Context.MODE_PRIVATE);
        return (sharedPreferences.getInt(key, _default));
    }
}
