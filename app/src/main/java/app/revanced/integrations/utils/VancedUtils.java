package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.security.SecureRandom;

public class VancedUtils {

    private VancedUtils() {
    }

    public static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static int countMatches(CharSequence seq, char c) {
        int count = 0;
        for (int i = 0; i < seq.length(); i++) {
            if (seq.charAt(i) == c)
                count++;
        }
        return count;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return (version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ("17.23.35");
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}