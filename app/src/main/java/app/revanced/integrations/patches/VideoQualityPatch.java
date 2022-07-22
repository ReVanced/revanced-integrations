package app.revanced.integrations.patches;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class VideoQualityPatch {

    public static final int[] videoResolutions = {0, 144, 240, 360, 480, 720, 1080, 1440, 2160};
    private static Boolean userChangedQuality = false;

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface, String qIndexMethod) {
        int preferredQuality;
        Field[] fields;
        if (!ReVancedUtils.isNewVideoStarted() || userChangedQuality || qInterface == null) {
            if (SettingsEnum.DEBUG.getBoolean() && userChangedQuality) {
                LogHelper.debug(VideoQualityPatch.class, "Skipping quality change because user changed it: " + quality);
            }
            userChangedQuality = false;
            return quality;
        }
        ReVancedUtils.setNewVideo(false);
        LogHelper.debug(VideoQualityPatch.class, "Quality: " + quality);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(VideoQualityPatch.class, "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (isConnectedWifi(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_WIFI.getInt();
            LogHelper.debug(VideoQualityPatch.class, "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (isConnectedMobile(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_MOBILE.getInt();
            LogHelper.debug(VideoQualityPatch.class, "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug(VideoQualityPatch.class, "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        Class<?> intType = Integer.TYPE;
        ArrayList<Integer> iStreamQualities = new ArrayList<>();
        try {
            for (Object streamQuality : qualities) {
                for (Field field : streamQuality.getClass().getFields()) {
                    if (field.getType().isAssignableFrom(intType)) {
                        int value = field.getInt(streamQuality);
                        if (field.getName().length() <= 2) {
                            iStreamQualities.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Collections.sort(iStreamQualities);
        int index = 0;
        for (int streamQuality2 : iStreamQualities) {
            LogHelper.debug(VideoQualityPatch.class, "Quality at index " + index + ": " + streamQuality2);
            index++;
        }
        for (Integer iStreamQuality : iStreamQualities) {
            int streamQuality3 = iStreamQuality;
            if (streamQuality3 <= preferredQuality) {
                quality = streamQuality3;
            }
        }
        if (quality == -2) {
            return quality;
        }
        int qualityIndex = iStreamQualities.indexOf(quality);
        LogHelper.debug(VideoQualityPatch.class, "Index of quality " + quality + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod(qIndexMethod, Integer.TYPE);
            LogHelper.debug(VideoQualityPatch.class, "Method is: " + qIndexMethod);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.debug(VideoQualityPatch.class, "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(VideoQualityPatch.class, "Failed to set quality", ex);
            return qualityIndex;
        }
    }

    public static void userChangedQuality() {
        userChangedQuality = true;
    }


    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected() && info.getType() == 1;
    }

    private static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected() && info.getType() == 0;
    }
    
}
