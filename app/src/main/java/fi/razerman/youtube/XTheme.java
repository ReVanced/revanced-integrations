package fi.razerman.youtube;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import android.util.TypedValue;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.log.LogHelper;
import app.revanced.integrations.settings.Settings;
import fi.razerman.youtube.Helpers.ColorRef;

/* loaded from: classes6.dex */
public class XTheme {
    static boolean ENABLE_COLOR_OVERRIDE = false;

    public static int SearchIconColor(int original) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("XTheme", "Context is null, returning " + original);
            return original;
        }
        try {
            return context.getResources().getColor(context.getResources().getIdentifier("custom_search_color", "color", Settings.getPackageName()));
        } catch (Resources.NotFoundException e) {
            return original;
        }
    }

    public static int RefreshIconColor(int original) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("XTheme", "Context is null, returning " + original);
            return original;
        }
        try {
            return context.getResources().getColor(context.getResources().getIdentifier("custom_refresh_color", "color", Settings.getPackageName()));
        } catch (Resources.NotFoundException e) {
            return original;
        }
    }

    public static int RefreshIconColor(Context context, int attributeId) {
        return getColorAttrContext(context, attributeId);
    }

    public static int getColorAttrContext(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        try {
            int color = context.getResources().getColor(colorRes);
            return color;
        } catch (Resources.NotFoundException e) {
            Log.w("Settings", "Not found color resource by id: " + colorRes);
            return -328966;
        }
    }

    public static int getColorAttrActivity(Activity context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        try {
            return context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("Settings", "Not found color resource by id: " + colorRes);
            return -328966;
        }
    }

    public static int PinnedMessageColor(int original) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("XTheme", "Context is null, returning " + original);
            return original;
        }
        try {
            return context.getResources().getColor(context.getResources().getIdentifier("custom_pinned_color", "color", Settings.getPackageName()));
        } catch (Resources.NotFoundException e) {
            return original;
        }
    }

    public static int getTheme(SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean("app_theme_dark", false)) {
            return 2;
        }
        if (sharedPreferences.getBoolean("app_theme_dark", false)) {
            return 3;
        }
        return 1;
    }

    private static boolean isDarkTheme() {
        return Settings.getThemeStatus();
    }

    public static int foregroundColorOverride(int original) {
        if (!ENABLE_COLOR_OVERRIDE) {
            return original;
        }
        LogHelper.debug("XTheme", "foregroundColorOverride: " + original);

        switch (original) {
            case -16359468:
                int returnValue3 = ColorRef.color("vanced_link_text_light", -16359468);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning blue: " + returnValue3);
                return returnValue3;
            case -15527149:
                int returnValue2 = isDarkTheme() ? ColorRef.color("vanced_text_secondary_dark", -15527149) : ColorRef.color("vanced_text_primary_light", -15527149);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning black: " + returnValue2);
                return returnValue2;
            case -12671233:
                int returnValue4 = ColorRef.color("vanced_link_text_dark", -12671233);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning blue: " + returnValue4);
                return returnValue4;
            case -10461088:
                int returnValue5 = ColorRef.color("vanced_text_accent_light", -10461088);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning grey: " + returnValue5);
                return returnValue5;
            case -5592406:
                int returnValue6 = ColorRef.color("vanced_text_accent_dark", -5592406);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning grey: " + returnValue6);
                return returnValue6;
            case -1:
                int returnValue = isDarkTheme() ? ColorRef.color("vanced_text_primary_dark", -1) : ColorRef.color("vanced_text_secondary_light", -1);
                LogHelper.debug("XTheme", "foregroundColorOverride - returning white: " + returnValue);
                return returnValue;
            default:
                LogHelper.debug("XTheme", "foregroundColorOverride - returning original: " + original);
                return original;
        }
    }
}
