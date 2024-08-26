package app.revanced.integrations.youtube.patches.spoof;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

public class DeviceHardwareSupport {
    private static final boolean DEVICE_HAS_HARDWARE_DECODING_VP9 = deviceHasVP9HardwareDecoding();
    private static final boolean DEVICE_HAS_HARDWARE_DECODING_AV1 = deviceHasAV1HardwareDecoding();

    private static boolean deviceHasVP9HardwareDecoding() {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);

        for (MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
            final boolean isHardwareAccelerated = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ? codecInfo.isHardwareAccelerated()
                    : !codecInfo.getName().startsWith("OMX.google"); // Software decoder.
            if (isHardwareAccelerated && !codecInfo.isEncoder()) {
                for (String type : codecInfo.getSupportedTypes()) {
                    if (type.equalsIgnoreCase("video/x-vnd.on2.vp9")) {
                        Logger.printDebug(() -> "Device supports VP9 hardware decoding.");
                        return true;
                    }
                }
            }
        }

        Logger.printDebug(() -> "Device does not support VP9 hardware decoding.");
        return false;
    }

    private static boolean deviceHasAV1HardwareDecoding() {
        // It appears all devices with hardware AV1 are also Android 10 or newer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);

            for (MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
                if (codecInfo.isHardwareAccelerated() && !codecInfo.isEncoder()) {
                    for (String type : codecInfo.getSupportedTypes()) {
                        if (type.equalsIgnoreCase("video/av01")) {
                            Logger.printDebug(() -> "Device supports AV1 hardware decoding.");
                            return true;
                        }
                    }
                }
            }
        }

        Logger.printDebug(() -> "Device does not support AV1 hardware decoding.");
        return false;
    }

    public static boolean allowVP9() {
        return DEVICE_HAS_HARDWARE_DECODING_VP9 && !Settings.SPOOF_CLIENT_FORCE_AVC.get();
    }

    public static boolean allowAV1() {
        return allowVP9() && DEVICE_HAS_HARDWARE_DECODING_AV1;
    }
}
