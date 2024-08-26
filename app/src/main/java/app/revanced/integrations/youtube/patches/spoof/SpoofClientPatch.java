package app.revanced.integrations.youtube.patches.spoof;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.chromium.net.ExperimentalUrlRequest;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.patches.BackgroundPlaybackPatch;
import app.revanced.integrations.youtube.patches.spoof.requests.StreamingDataRequester;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofClientPatch {
    private static final boolean SPOOF_CLIENT = Settings.SPOOF_CLIENT.get();
    private static final SpoofClientStrategy SPOOF_STRATEGY = Settings.SPOOF_CLIENT_STRATEGY.get();
    @Nullable
    private static final ClientType SPOOF_CLIENT_TYPE = SPOOF_CLIENT ? SPOOF_STRATEGY.clientType : null;
    private static final boolean SPOOF_STREAM = SPOOF_CLIENT && SPOOF_STRATEGY == SpoofClientStrategy.REPLACE_STREAMS;

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    private static volatile Future<ByteBuffer> currentVideoStream;

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (SPOOF_CLIENT) {
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
        if (SPOOF_CLIENT) {
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
    public static boolean isClientTypeSpoofingEnabled() {
        return SPOOF_CLIENT_TYPE != null;
    }

    /**
     * Injection point.
     */
    public static boolean isSpoofStreamEnabled() {
        return SPOOF_STREAM;
    }

    /**
     * Injection point.
     */
    public static int getClientTypeId(int originalClientTypeId) {
        return SPOOF_CLIENT_TYPE != null ? SPOOF_CLIENT_TYPE.id : originalClientTypeId;
    }

    /**
     * Injection point.
     */
    public static String getClientVersion(String originalClientVersion) {
        return SPOOF_CLIENT_TYPE != null ? SPOOF_CLIENT_TYPE.appVersion : originalClientVersion;
    }

    /**
     * Injection point.
     */
    public static String getClientModel(String originalClientModel) {
        return SPOOF_CLIENT_TYPE != null ? SPOOF_CLIENT_TYPE.model : originalClientModel;
    }

    /**
     * Injection point.
     * Fix video qualities missing, if spoofing to iOS by using the correct client OS version.
     */
    public static String getOsVersion(String originalOsVersion) {
        return SPOOF_CLIENT_TYPE != null ? SPOOF_CLIENT_TYPE.osVersion : originalOsVersion;
    }

    /**
     * Injection point.
     */
    public static boolean enablePlayerGesture(boolean original) {
        return SPOOF_CLIENT && SPOOF_STRATEGY.enablePlayerGesture || original;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, the playback speed menu is missing from the player response.
     * Return true to force create the playback speed menu.
     */
    public static boolean forceCreatePlaybackSpeedMenu(boolean original) {
        return SPOOF_CLIENT && SPOOF_STRATEGY.forceCreatePlaybackSpeedMenu || original;
    }

    /**
     * Injection point.
     * When spoofing the client to iOS, background audio only playback of livestreams fails.
     * Return true to force enable audio background play.
     */
    public static boolean overrideBackgroundAudioPlayback() {
        return SPOOF_CLIENT && SPOOF_STRATEGY.overrideBackgroundAudioPlayback
                && BackgroundPlaybackPatch.playbackIsNotShort();
    }

    /**
     * Injection point.
     */
    public static ExperimentalUrlRequest overrideUserAgent(ExperimentalUrlRequest.Builder builder,
                                                           String url, Map<String, String> playerHeaders) {
        if (SPOOF_CLIENT) {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (path != null && path.contains("player") && !path.contains("heartbeat")) {
            	if (SPOOF_CLIENT_TYPE != null) {
                    Logger.printDebug(() -> "Overriding user agent for /player call");
                    builder.addHeader("User-Agent", SPOOF_CLIENT_TYPE.userAgent);
                } else {
                    // Spoof stream.
                    String videoId = uri.getQueryParameter("id");
                    currentVideoStream = StreamingDataRequester.fetch(videoId, playerHeaders);
                }
            }
        }

        return builder.build();
    }

    /**
     * Injection point.
     * Fix playback by replace the streaming data.
     * Called after {@link #overrideUserAgent(ExperimentalUrlRequest.Builder, String, Map)}.
     */
    @Nullable
    public static ByteBuffer getStreamingData(String videoId) {
        if (SPOOF_STREAM) {
            try {
                Utils.verifyOffMainThread();

                var future = currentVideoStream;
                if (future != null) {
                    final long maxSecondsToWait = 20;
                    var stream = future.get(maxSecondsToWait, TimeUnit.SECONDS);
                    if (stream != null) {
                        Logger.printDebug(() -> "Overriding video stream");
                        return stream;
                    }

                    Logger.printDebug(() -> "Not overriding streaming data (video stream is null)");
                }
            } catch (TimeoutException ex) {
                Logger.printInfo(() -> "getStreamingData timed out", ex);
            } catch (InterruptedException ex) {
                Logger.printException(() -> "getStreamingData interrupted", ex);
                Thread.currentThread().interrupt(); // Restore interrupt status flag.
            } catch (ExecutionException ex) {
                Logger.printException(() -> "getStreamingData failure", ex);
            }
        }

        return null;
    }

    /**
     * Injection point.
     * Called after {@link #getStreamingData(String)}.
     */
    public static byte[] removeVideoPlaybackPostBody(Uri uri, int method, byte[] postData) {
        if (SPOOF_STREAM) {
            try {
                final int methodPost = 2;
                if (method == methodPost) {
                    String path = uri.getPath();
                    String clientName = "c";
                    final boolean iosClient = ClientType.IOS.name().equals(uri.getQueryParameter(clientName));
                    if (iosClient && path != null && path.contains("videoplayback")) {
                        return null;
                    }
                }
            }  catch (Exception ex) {
                Logger.printException(() -> "removeVideoPlaybackPostBody failure", ex);
            }
        }

        return postData;
    }

    public enum SpoofClientStrategy {
        REPLACE_STREAMS(null,
                false,
                false,
                false),
        CLIENT_IOS(ClientType.IOS,
                true,
                true,
                true),
        CLIENT_ANDROID_VR(ClientType.ANDROID_VR,
                false,
                false,
                false);

        private final ClientType clientType;
        private final boolean forceCreatePlaybackSpeedMenu;
        private final boolean overrideBackgroundAudioPlayback;
        private final boolean enablePlayerGesture;

        SpoofClientStrategy(ClientType  clientType,
                            boolean forceCreatePlaybackSpeedMenu,
                            boolean overrideBackgroundAudioPlayback,
                            boolean enablePlayerGesture) {
            this.clientType = clientType;
            this.forceCreatePlaybackSpeedMenu = forceCreatePlaybackSpeedMenu;
            this.overrideBackgroundAudioPlayback = overrideBackgroundAudioPlayback;
            this.enablePlayerGesture = enablePlayerGesture;
        }
    }

    public static final class ForceAVCAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            if (!Settings.SPOOF_CLIENT.get()) return false;

            SpoofClientStrategy strategy = Settings.SPOOF_CLIENT_STRATEGY.get();
            return strategy == SpoofClientStrategy.CLIENT_IOS || strategy == SpoofClientStrategy.REPLACE_STREAMS;
        }
    }
}
