package app.revanced.integrations.patches.playback.quality;

import static app.revanced.integrations.utils.ReVancedUtils.NetworkType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class DefaultVideoQualityPatch {
    private static final int NO_DEFAULT_QUALITY_SET = (int) SettingsEnum.VIDEO_QUALITY_DEFAULT_WIFI.defaultValue;
    private static final SettingsEnum wifiQualitySetting = SettingsEnum.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final SettingsEnum mobileQualitySetting = SettingsEnum.VIDEO_QUALITY_DEFAULT_MOBILE;

    private static int selectedQuality1 = NO_DEFAULT_QUALITY_SET;
    private static boolean newVideo;
    private static boolean userChangedQuality;

    private static void changeDefaultQuality(int defaultQuality) {
        NetworkType networkType = ReVancedUtils.getNetworkType();
        if (networkType == NetworkType.NONE) {
            ReVancedUtils.showToastShort("No internet connection");
            return;
        }
        String networkTypeMessage;
        if (networkType == NetworkType.MOBILE) {
            mobileQualitySetting.saveValue(defaultQuality);
            networkTypeMessage = "mobile";
        } else {
            wifiQualitySetting.saveValue(defaultQuality);
            networkTypeMessage = "WIFI";
        }
        ReVancedUtils.showToastShort("Changing default " + networkTypeMessage
                + " quality to: " + defaultQuality);
    }

    /**
     * Injection point.
     */
    public static int setVideoQuality(Object[] qualities, final int originalQuality, Object qInterface, String qIndexMethod) {
        try {
            if (!(newVideo || userChangedQuality) || qInterface == null) {
                return originalQuality;
            }

            Class<?> intType = Integer.TYPE;
            List<Integer> iStreamQualities = new ArrayList<>();
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
                // edit: what could be caught here?
            }
            Collections.sort(iStreamQualities);
            int index = 0;
            if (userChangedQuality) {
                for (int convertedQuality : iStreamQualities) {
                    final int selectedQuality2 = qualities.length - selectedQuality1 + 1;
                    index++;
                    if (selectedQuality2 == index) {
                        final int indexToLog = index; // must be final for lambda
                        LogHelper.printDebug(() -> "Quality index is: " + indexToLog + " and corresponding value is: " + convertedQuality);
                        changeDefaultQuality(convertedQuality);
                        userChangedQuality = false;
                        return selectedQuality2;
                    }
                }
            }
            newVideo = false;
            LogHelper.printDebug(() -> "Quality: " + originalQuality);

            var networkType = ReVancedUtils.getNetworkType();
            if (networkType == NetworkType.NONE) {
                LogHelper.printDebug(() -> "No Internet connection");
                return originalQuality;
            }

            final int preferredQuality;
            if (networkType == NetworkType.MOBILE) {
                preferredQuality = mobileQualitySetting.getInt();
            } else {
                preferredQuality = wifiQualitySetting.getInt();
            }
            if (preferredQuality == NO_DEFAULT_QUALITY_SET) {
                return originalQuality;
            }

            int quality = originalQuality;
            for (Integer iStreamQuality : iStreamQualities) {
                if (iStreamQuality <= preferredQuality) {
                    quality = iStreamQuality;
                }
            }
            if (quality == -2) return quality; // edit: ?

            final int qualityIndex = iStreamQualities.indexOf(quality);
            final int qualityToLog = quality;
            LogHelper.printDebug(() -> "Index of quality " + qualityToLog + " is " + qualityIndex);

            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod(qIndexMethod, Integer.TYPE);
            LogHelper.printDebug(() -> "Method is: " + qIndexMethod);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.printDebug(() -> "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to set quality", ex);
            return originalQuality;
        }
    }

    /**
     * Injection point.
     */
    public static void userChangedQuality(int selectedQuality) {
        if (!SettingsEnum.VIDEO_QUALITY_REMEMBER_LAST_SELECTED.getBoolean()) return;

        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(String videoId) {
        newVideo = true; // FIXME: this method can be called multiple times for the same video playback
    }
}
