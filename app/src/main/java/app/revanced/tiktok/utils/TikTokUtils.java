package app.revanced.tiktok.utils;

import android.annotation.SuppressLint;
import android.content.Context;

public class TikTokUtils {

    //Used by TiktokIntegrations patch
    public static Context context;

    //Used by TiktokIntegrations patch
    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException(TikTokUtils.class, "Context is null!");
        return null;
    }

    /**
     * Get resource id safely
     * @return May return 0 if resource not found or context not attached
     */
    @SuppressLint("DiscouragedApi")
    public static int getIdentifier(String name, String defType) {
        Context context = getAppContext();
        if (context == null) {
            return 0;
        }
        int resId = context.getResources().getIdentifier(name, defType, context.getPackageName());
        if(resId == 0) {
            LogHelper.printException(TikTokUtils.class, "Resource '" + name + "' not found");
        }
        return resId;
    }

    public static int getStringId(String name) {
        return getIdentifier(name, "string");
    }

    public static String getString(String name) {
        if (context == null) {
            return name;
        }
        return context.getString(getStringId(name));
    }


}