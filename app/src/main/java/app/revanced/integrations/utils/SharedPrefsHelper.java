package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

import app.revanced.integrations.utils.LogHelper;


/* loaded from: classes6.dex */
public class SharedPrefsHelper {

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        try {
            if (context == null) {
                LogHelper.printException("XSharedPrefs", "context is null");
                return false;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            return sharedPreferences.getBoolean(key, defValue);
        } catch (Exception ex) {
            LogHelper.printException("XSharedPrefs", "Error getting boolean", ex);
            return defValue;
        }
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static void saveBoolean(Context context, String key, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static void saveInt(Context context, String key, Integer value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    public static String getString(Context context, String key, String _default) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        return sharedPreferences.getString(key, _default);
    }

    public static Boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static Boolean getBoolean(Context context, String key, Boolean _default) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        return sharedPreferences.getBoolean(key, _default);
    }

    public static Integer getInt(Context context, String key) {
        return getInt(context, key, -1);
    }

    public static Integer getInt(Context context, String key, Integer _default) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        return sharedPreferences.getInt(key, _default);
    }
}
