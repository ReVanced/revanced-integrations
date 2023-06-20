package app.revanced.tiktok.utils;

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
}