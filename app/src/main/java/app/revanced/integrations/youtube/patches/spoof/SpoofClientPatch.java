package app.revanced.integrations.youtube.patches.spoof;

import static app.revanced.integrations.youtube.patches.spoof.SpoofClientPatch.DeviceHardwareSupport.allowAV1;
import static app.revanced.integrations.youtube.patches.spoof.SpoofClientPatch.DeviceHardwareSupport.allowVP9;

import androidx.annotation.Nullable;
import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.os.Build;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.chromium.net.ExperimentalUrlRequest;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.patches.BackgroundPlaybackPatch;
import app.revanced.integrations.youtube.patches.spoof.requests.StreamingDataRequester;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT_ENABLED = Settings.SPOOF_CLIENT.get();
    private static final ClientType SPOOF_CLIENT_TYPE = Settings.SPOOF_CLIENT_TYPE.get();
    private static final boolean SPOOF_IOS = SPOOF_CLIENT_ENABLED && SPOOF_CLIENT_TYPE == ClientType.IOS;
    private static final boolean SPOOF_STREAM_ENABLED = Settings.SPOOF_STREAM.get();

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    /**
     * Streaming data store.
     */
    @Nullable
    private static CompletableFuture<ByteBuffer> streamingDataFuture;
    private static final ConcurrentHashMap<String, ByteBuffer> streamingDataCache = new ConcurrentHashMap<>();

    /**
     * Last video id prefetched. Field is to prevent prefetching the same video id multiple times in a row.
     */
    @Nullable
    private static volatile String lastPrefetchedVideoId;

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (SPOOF_CLIENT_ENABLED || SPOOF_STREAM_ENABLED) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking 'get_watch' by returning unreachable uri");

                    return UNREACHABLE_HOST_URI;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockGetWatchRequest failure", ex);
            }
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * <p>
     * Blocks /initplayback requests.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (SPOOF_CLIENT_ENABLED || SPOOF_STREAM_ENABLED) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    Logger.printDebug(() -> "Blocking 'initplayback' by returning unreachable url");

                    return UNREACHABLE_HOST_URI_STRING;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockInitPlaybackRequest failure", ex);
            }
        }

        return originalUrlString;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.id : originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.appVersion : originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static String getClientModel(String originalClientModel) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.model : originalClientModel;
    }

    /**
     * Injection point.
     * Fix video qualities missing, if spoofing to iOS by using the correct client OS version.
     */
    public static String getOsVersion(String originalOsVersion) {
        return SPOOF_CLIENT_ENABLED ? SPOOF_CLIENT_TYPE.osVersion : originalOsVersion;
    }

    /**
     * Injection point.
     */
    public static boolean enablePlayerGesture(boolean original) {
        return SPOOF_CLIENT_ENABLED || original;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return SPOOF_CLIENT_ENABLED;
    }

    /**
     * Injection point.
     */
    public static boolean isSpoofStreamEnabled() {
        return SPOOF_STREAM_ENABLED;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, the playback speed menu is missing from the player response.
     * Return true to force create the playback speed menu.
     */
    public static boolean forceCreatePlaybackSpeedMenu(boolean original) {
        return SPOOF_IOS || original;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, background audio only playback of livestreams fails.
     * Return true to force enable audio background play.
     */
    public static boolean overrideBackgroundAudioPlayback() {
        return SPOOF_IOS && BackgroundPlaybackPatch.playbackIsNotShort();
    }

    /**
     * Injection point.
     * Fix video qualities missing, if spoofing to iOS by using the correct iOS user-agent.
     */
    public static ExperimentalUrlRequest overrideUserAgent(ExperimentalUrlRequest.Builder builder, String url, Map playerHeaders) {
        if (SPOOF_CLIENT_ENABLED || SPOOF_STREAM_ENABLED) {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (path != null && path.contains("player") && !path.contains("heartbeat")) {
            	if (SPOOF_CLIENT_ENABLED) {
                    return builder.addHeader("User-Agent", SPOOF_CLIENT_TYPE.userAgent).build();
                }
                if (SPOOF_STREAM_ENABLED) {
                    fetchStreamingData(uri.getQueryParameter("id"), playerHeaders);
                    return builder.build();
                }
            }
        }

        return builder.build();
    }

    /**
     * Injection point.
     * Fix playback by replace the streaming data.
     */
    @SuppressLint("NewApi")
    public static ByteBuffer getStreamingData(String videoId) {
        if (!SPOOF_STREAM_ENABLED) return null;

        if (streamingDataCache.containsKey(videoId)) {
            return streamingDataCache.get(videoId);
        }

        if (streamingDataFuture != null) {
            try {
                ByteBuffer byteBuffer = streamingDataFuture.get();
                if (byteBuffer != null) {
                    streamingDataCache.put(videoId, byteBuffer);
                    return byteBuffer;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Logger.printException(() -> "getStreamingData interrupted.", ex);
            } catch (ExecutionException ex) {
                Logger.printException(() -> "getStreamingData failure.", ex);
            }
        }

        return null;
    }

    /**
     * Injection point.
     */
    public static void fetchStreamingData(String videoId, Map playerHeaders) {
        if (SPOOF_STREAM_ENABLED) {
            if (videoId.equals(lastPrefetchedVideoId)) {
                return;
            }

            if (!streamingDataCache.containsKey(videoId)) {
                CompletableFuture<ByteBuffer> future = StreamingDataRequester.fetch(videoId, playerHeaders);
                streamingDataFuture = future;
            }
            lastPrefetchedVideoId = videoId;
        }
    }

    /**
     * Injection point.
     */
    public static byte[] removeVideoPlaybackPostBody(Uri uri, int method, byte[] postData) {
        if (!SPOOF_STREAM_ENABLED) return postData;

        String path = uri.getPath();
        boolean iosClient = ClientType.IOS.name().equals(uri.getQueryParameter("c"));
        if (path != null && path.contains("videoplayback") && method == 2 && iosClient) {
            return null;
        }

        return postData;
    }

    // Must check for device features in a separate class and cannot place this code inside
    // the Patch or ClientType enum due to cyclic Setting references.
    static class DeviceHardwareSupport {
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

        static boolean allowVP9() {
            return DEVICE_HAS_HARDWARE_DECODING_VP9 && !Settings.SPOOF_CLIENT_IOS_FORCE_AVC.get();
        }

        static boolean allowAV1() {
            return allowVP9() && DEVICE_HAS_HARDWARE_DECODING_AV1;
        }
    }

    public enum ClientType {
        // https://dumps.tadiphone.dev/dumps/oculus/eureka
        IOS(5,
                // iPhone 15 supports AV1 hardware decoding.
                // Only use if this Android device also has hardware decoding.
                allowAV1()
                        ? "iPhone16,2"  // 15 Pro Max
                        : "iPhone11,4", // XS Max
                // iOS 14+ forces VP9.
                allowVP9()
                        ? "17.5.1.21F90"
                        : "13.7.17H35",
                allowVP9()
                        ? "com.google.ios.youtube/19.10.7 (iPhone; U; CPU iOS 17_5_1 like Mac OS X)"
                        : "com.google.ios.youtube/19.10.7 (iPhone; U; CPU iOS 13_7 like Mac OS X)",
                // Version number should be a valid iOS release.
                // https://www.ipa4fun.com/history/185230
                "19.10.7"
        ),
        ANDROID_VR(28,
                "Quest 3",
                "12",
                "com.google.android.apps.youtube.vr.oculus/1.56.21 (Linux; U; Android 12; GB) gzip",
                "1.56.21"
        ),
        ANDROID(3,
                Build.MODEL,
                Build.VERSION.RELEASE,
                String.format("com.google.android.youtube/%s (Linux; U; Android %s; GB) gzip",
                        Utils.getAppVersionName(), Build.VERSION.RELEASE),
                Utils.getAppVersionName()
        );

        /**
         * YouTube
         * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
         */
        public final int id;

        /**
         * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.model)
         */
        public final String model;

        /**
         * Device OS version.
         */
        public final String osVersion;

        /**
         * Player user-agent.
         */
        public final String userAgent;

        /**
         * App version.
         */
        public final String appVersion;

        ClientType(int id, String model, String osVersion, String userAgent, String appVersion) {
            this.id = id;
            this.model = model;
            this.osVersion = osVersion;
            this.userAgent = userAgent;
            this.appVersion = appVersion;
        }
    }

    public static final class ForceiOSAVCAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.SPOOF_CLIENT.get() && Settings.SPOOF_CLIENT_TYPE.get() == ClientType.IOS;
        }
    }
}
