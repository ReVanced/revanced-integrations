package app.revanced.integrations;

import android.content.Context;

import app.revanced.integrations.log.LogHelper;


public class Globals {
    public static Context context;

    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException("Globals", "Context is null!");
        return null;
    }
}