package app.revanced.tiktok.utils;

import android.content.Context;

public class ReVancedTikTokUtils {

    //Used by TiktokIntegrations patch
    public static Context context;

    //Used by TiktokIntegrations patch
    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException(ReVancedTikTokUtils.class, "Context is null!");
        return null;
    }
}