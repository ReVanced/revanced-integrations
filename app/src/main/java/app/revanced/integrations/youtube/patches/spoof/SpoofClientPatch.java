package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;
import android.os.Build;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT_ENABLED = Settings.SPOOF_CLIENT.get();
    private static final ClientType SPOOF_CLIENT_TYPE = Settings.SPOOF_CLIENT_USE_IOS.get() ? ClientType.IOS : ClientType.ANDROID_VR;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (SPOOF_CLIENT_ENABLED) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking: " + playerRequestUri + " by returning unreachable uri");

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
     *
     * Blocks /initplayback requests.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (SPOOF_CLIENT_ENABLED) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    Logger.printDebug(() -> "Blocking: " + originalUrlString + " by returning unreachable url");

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
        if (SPOOF_CLIENT_ENABLED) {
            return SPOOF_CLIENT_TYPE.id;
        }

        return originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        if (SPOOF_CLIENT_ENABLED) {
            return SPOOF_CLIENT_TYPE.version;
        }

        return originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static String getClientModel(String originalClientModel) {
        if (SPOOF_CLIENT_ENABLED) {
            return SPOOF_CLIENT_TYPE.model;
        }

        return originalClientModel;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return SPOOF_CLIENT_ENABLED;
    }

    private enum ClientType {
        // https://dumps.tadiphone.dev/dumps/oculus/monterey/-/blob/vr_monterey-user-7.1.1-NGI77B-256550.6810.0-release-keys/system/system/build.prop
        // version 1.37 is not the latest, but it works with livestream audio only playback.
        ANDROID_VR(28, "Quest", "1.37"),
        // 15,3 = iPhone 14 Pro Max.  Should not use iPhone 15 Pro or
        // any other Apple device with AV1 hardware acceleration,
        // as that might give a higher chance of AV1 video streams
        // (Most Android devices use software AV1 decoding).
        //
        // Version number should be a valid iOS release.
        // https://www.ipa4fun.com/history/185230
        IOS(5, "iPhone15,3", "19.10.7");

        /**
         * YouTube
         * <a href="https://github.com/zerodytrash/YouTube-Internal-Clients?tab=readme-ov-file#clients">client type</a>
         */
        final int id;

        /**
         * Device model, equivalent to {@link Build#MODEL} (System property: ro.product.model)
         */
        final String model;

        /**
         * App version.
         */
        final String version;

        ClientType(int id, String model, String version) {
            this.id = id;
            this.model = model;
            this.version = version;
        }
    }
}
