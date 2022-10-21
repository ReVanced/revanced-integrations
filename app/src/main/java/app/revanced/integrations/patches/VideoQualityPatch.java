package app.revanced.integrations.patches;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class VideoQualityPatch {

    public static int selectedQuality1 = -2;
    private static Boolean newVideo = false;
    private static Boolean userChangedQuality = false;

    public static void changeDefaultQuality(int defaultQuality) {
        Context context = ReVancedUtils.getContext();

        // Do not remember a **new** quality if REMEMBER_VIDEO_QUALITY is true
        if (SettingsEnum.REMEMBER_VIDEO_QUALITY.getBoolean()) {
            userChangedQuality = false;
            return;
        }

        if (isConnectedWifi(context)) {
            try {
                SettingsEnum.DEFAULT_VIDEO_QUALITY_WIFI.saveValue(defaultQuality);
                SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "revanced_pref_video_quality_wifi", defaultQuality + "");
            } catch (Exception ex) {
                LogHelper.printException(VideoQualityPatch.class, "Failed to change default WI-FI quality" + ex);
                Toast.makeText(context, str("revanced_video_quality_wifi_error"), Toast.LENGTH_SHORT).show();
            }
            LogHelper.debug(VideoQualityPatch.class, "Changing default Wi-Fi quality to: " + defaultQuality);
            Toast.makeText(context, str("revanced_video_quality_wifi") + "" + defaultQuality + "p", Toast.LENGTH_SHORT).show();
        } else if (isConnectedMobile(context)) {
            try {
                SettingsEnum.DEFAULT_VIDEO_QUALITY_MOBILE.saveValue(defaultQuality);
                SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "revanced_pref_video_quality_mobile", defaultQuality + "");
            } catch (Exception ex) {
                LogHelper.debug(VideoQualityPatch.class, "Failed to change default mobile data quality" + ex);
                Toast.makeText(context, str("revanced_video_quality_mobile_error"), Toast.LENGTH_SHORT).show();
            }
            LogHelper.debug(VideoQualityPatch.class, "Changing default mobile data quality to:" + defaultQuality);
            Toast.makeText(context, str("revanced_video_quality_mobile") + "" + defaultQuality + "p", Toast.LENGTH_SHORT).show();
        } else {
            LogHelper.debug(VideoQualityPatch.class, "No internet connection.");
            Toast.makeText(context, str("revanced_video_quality_internet_error"), Toast.LENGTH_SHORT).show();
        }
        userChangedQuality = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface, String qIndexMethod) {
        int preferredQuality;
        Field[] fields;
        if (!newVideo || qInterface == null) {
            return quality;
        }
        Class<?> intType = Integer.TYPE;
        ArrayList<Integer> iStreamQualities = new ArrayList<>();
        try {
            for (Object streamQuality : qualities) {
                for (Field field : streamQuality.getClass().getFields()) {
                    if (field.getType().isAssignableFrom(intType)) {  // converts quality index to actual readable resolution
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
        if (userChangedQuality) {
            for (int convertedQuality : iStreamQualities) {
                int selectedQuality2 = qualities.length - selectedQuality1 + 1;
                index++;
                if (selectedQuality2 == index) {
                    LogHelper.debug(VideoQualityPatch.class, "Quality index is: " + index + " and corresponding value is: " + convertedQuality);
                    changeDefaultQuality(convertedQuality);
                    return selectedQuality2;
                }
            }
        }
        newVideo = false;
        LogHelper.debug(VideoQualityPatch.class, "Quality: " + quality);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(VideoQualityPatch.class, "Context is null or settings not initialized, returning quality: " + quality);
            newVideo = true;
            return quality;
        }
        if (isConnectedWifi(context)) {
            preferredQuality = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "revanced_pref_video_quality_wifi", -2);
            LogHelper.debug(VideoQualityPatch.class, "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (isConnectedMobile(context)) {
            preferredQuality = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "revanced_pref_video_quality_mobile", -2);
            LogHelper.debug(VideoQualityPatch.class, "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug(VideoQualityPatch.class, "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
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
            if (qualityIndex == -2) {
                newVideo = true;
            }
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(VideoQualityPatch.class, "Failed to set quality", ex);
            Toast.makeText(context, str("revanced_video_quality_common_error"), Toast.LENGTH_SHORT).show();
            return qualityIndex;
        }
    }

    public static void userChangedQuality(int selectedQuality) {
        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
    }

    public static void newVideoStarted(String videoId) {
        if (videoId == null) return;
        newVideo = true;
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
