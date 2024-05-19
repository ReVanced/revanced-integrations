package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ClientSpoofPatch {
    private static final boolean CLIENT_SPOOF_ENABLED = Settings.SPOOF_CLIENT.get();
    private static final boolean CLIENT_SPOOF_USE_IOS = Settings.SPOOF_CLIENT_USE_IOS.get();

    private static final ClientType CLIENT_TYPE = CLIENT_SPOOF_USE_IOS ? ClientType.IOS : ClientType.ANDROID_TESTSUITE;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URL = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URL);


    /**
     * Injection point.
     * Blocks /get_watch requests by returning a localhost URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return Localhost URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        try {
            if (isClientSpoofingEnabled()) {
                String path = playerRequestUri.getPath();
                if (path != null && path.contains("get_watch")) {
                    return UNREACHABLE_HOST_URI;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "blockGetWatchRequest failure", ex);
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * <p>
     * Blocks /initplayback requests.
     * For iOS, an unreachable host URL can be used, but for Android Testsuite, this is not possible.
     */
    public static String blockInitPlaybackRequest(String originalUrl) {
        if (isClientSpoofingEnabled()) {
            if (CLIENT_SPOOF_USE_IOS) {
                return UNREACHABLE_HOST_URL;
            } else {
                // TODO: Ideally, a local proxy could be setup and block the request the same way as Burp Suite is capable of
                //  because that way the request is never sent to YouTube unnecessarily.
                //  Just using localhost does unfortunately not work.
                return Uri.parse(originalUrl).buildUpon().clearQuery().build().toString();
            }
        }

        return originalUrl;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        if (isClientSpoofingEnabled()) {
            return CLIENT_TYPE.id;
        }

        return originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        if (isClientSpoofingEnabled()) {
            return CLIENT_TYPE.version;
        }

        return originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static boolean isClientSpoofingEnabled() {
        return CLIENT_SPOOF_ENABLED;
    }

    enum ClientType {
        ANDROID_TESTSUITE(30, "1.9"),
        IOS(5, Utils.getAppVersionName());

        final int id;
        final String version;

        ClientType(int id, String version) {
            this.id = id;
            this.version = version;
        }
    }
}
