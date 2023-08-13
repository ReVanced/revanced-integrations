package app.revanced.integrations.patches;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.chromium.net.UrlResponseInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Integration of two different thumbnail replacement schemes.
 *
 * DeArrow, using the thumbnail api. Gives the best thumbnail quality
 * and uses either a crowd sourced chosen video capture time, or a random screen grab time.
 * Available for many, but not all videos.
 *
 *
 * Alternate YouTube provided thumbnails, showing the beginning/middle/end of the video.
 * (ie: sd1.jpg, sd2.jpg, sd3.jpg).  Available for all videos.
 *
 * Alt thumbnails has an additional option to use 'fast' thumbnails,
 * where it forces sd thumbnail quality and skips verifying if an alt thumbnail image exists.
 * Thumbnails will initially fail to load for all live streams, unreleased, and occasionally for very old videos.
 * If a failed thumbnail load is reloaded ((ie: scroll off then on screen), then the original thumbnail
 * is used instead.  Fast thumbnails requires using SD or lower thumbnail resolution,
 * because many videos do not have 720 or better and a noticeable number of regular videos fail to load.
 *
 * Both strategies can be used together (try to use DeArrow, and if not available then fall back on using alt thumbnails),
 * and it works as expected. But at this time, to keep things simpler,
 * the UI prevents using both together (one or the other can be enabled, but not both).
 */
public final class ClickbaitRemoverPatch {

    private static final String DEARROW_API_SERVER_ADDRESS = "https://dearrow-thumb.ajay.app";

    /**
     * YouTube thumbnail URL prefix. Can be '/vi/' or '/vi_webp/'
     */
    public static final String YOUTUBE_THUMBNAIL_PREFIX = "https://i.ytimg.com/vi";

    /**
     * Number of samples to use for calculating {@link #rollingAverageOfAPISuccess}.
     * A larger value means more failures are needed before temporarily turning off API access.
     */
    private static final int ROLLING_AVERAGE_NUMBER_OF_SAMPLES = 20;

    /**
     * A rolling average calculating the number of successful vs unsuccessful DeArrow api calls.
     * a value of 1.0 means success, 0.0 means unsuccessful.
     *
     * If this rolling average reaches 0.5, then API calls are temporarily turned off for a short time.
     */
    private static float rollingAverageOfAPISuccess = 1.0f;

    /**
     * If API fails too many times in a row, then temporarily turn off for this amount of time.
     */
    private static final long API_FAILURE_BACKOFF_TIME_MILLISECONDS = 10 * 60 * 1000; // 10 minutes.

    /**
     * System time in milliseconds of when api calls can resume.
     */
    private static long timeToResumeApiCalls;

    /**
     * Cache used to verify if alternate thumbnails exists for a given video id.
     */
    private static final LinkedHashMap<String, Boolean> altVideoIdLookup = new LinkedHashMap<>(100) {
        private static final int CACHE_LIMIT = 1000;
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > CACHE_LIMIT; // Evict oldest entry if over the cache limit.
        }
    };

    static {
        // Fix any bad imported data.
        final int altThumbnailType = SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.getInt();
        if (altThumbnailType < 1 || altThumbnailType > 3) {
            LogHelper.printException(() -> "Invalid Clickbait alt thumbnail type: " + altThumbnailType);
            SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.saveValue(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.defaultValue);
        }
    }

    /**
     * Reset success/failure internal statistics, which forces API to resume if temporarily turned off.
     * Called if the user turns DeArrow off and then back on.
     */
    public static void resetAPISuccessRate() {
        if (timeToResumeApiCalls != 0) {
            LogHelper.printDebug(() -> "Clearing time to resume value (API is now available again)");
        }
        timeToResumeApiCalls = 0;
        rollingAverageOfAPISuccess = 1.0f;
    }

    private static void setVideoIdHasAltYouTubeThumbnails(@NonNull String videoId, boolean isValid) {
        altVideoIdLookup.put(Objects.requireNonNull(videoId), isValid);
        if (!isValid) {
            LogHelper.printDebug(() -> "Alternate thumbnails not available for video: " + videoId);
        }
    }

    /**
     * Verify if a video alt thumbnail exists.  Does so by making a minimal HEAD http request.
     *
     * Known limitation of this implementation:
     *
     * If a video does not have alt thumbnails for it's original thumbnail size (ie: hq720_2.jpg),
     * then this will instead use the original thumbnail.  This situation is somewhat rare.
     * This could try verifying a lower resolution thumbnail (ie: sd2.jpg), which almost always works.
     * But making this change would make this class more complicated, and the videos this is affected by
     * are almost always very old or very low view count.
     */
    private static boolean verifyAltYouTubeThumbnailsExist(@NonNull String videoId, @NonNull String imageUrl) {
        Boolean hasAltThumbnails = altVideoIdLookup.get(videoId);
        if (hasAltThumbnails == null) {
            if (SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_FAST_QUALITY.getBoolean()) {
                // In fast quality, skip checking if the alt thumbnail exists.
                // Instead, it only detects if an alt thumbnail has previously failed to laad.
                return true;
            }

            try {
                // The hooked code is running at a low priority, and it's slightly faster
                // to run the url connection thru the integrations thread pool which runs at the highest priority.
                final long start = System.currentTimeMillis();
                hasAltThumbnails = ReVancedUtils.submitOnBackgroundThread(() -> {
                    final int connectionTimeoutMillis = 5000;
                    HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.setConnectTimeout(connectionTimeoutMillis);
                    connection.setReadTimeout(connectionTimeoutMillis);
                    connection.setRequestMethod("HEAD");
                    // Even with a HEAD request, the response is the same size as a full GET request.
                    // Using an empty range fixes this.
                    connection.setRequestProperty("Range", "bytes=0-0");
                    final int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        String contentType = connection.getContentType();
                        return (contentType != null && contentType.startsWith("image"));
                    }
                    if (responseCode != HttpURLConnection.HTTP_NOT_FOUND) {
                        LogHelper.printDebug(() -> "Unexpected response code: " + responseCode + " for url: " + imageUrl);
                    }
                    return false;
                }).get();
                LogHelper.printDebug(() -> "Alt verification took: " + (System.currentTimeMillis() - start) + "ms");
            } catch (ExecutionException | InterruptedException ex) {
                LogHelper.printInfo(() -> "Could not verify alt url: " + imageUrl, ex);
                hasAltThumbnails = false;
            }
            setVideoIdHasAltYouTubeThumbnails(videoId, hasAltThumbnails);
        }
        return hasAltThumbnails;
    }

    /**
     * @return If Crowd sourced thumbnails are enabled, and API calls are not temporarily turned off.
     */
    private static boolean shouldUseDeArrowAPI() {
        if (!SettingsEnum.CLICKBAIT_DEARROW.getBoolean()) {
            return false;
        }
        if (timeToResumeApiCalls != 0) {
            if (System.currentTimeMillis() < timeToResumeApiCalls) {
                return false; // API temporarily turned off.
            }
            timeToResumeApiCalls = 0; // API calls can now resume.
            rollingAverageOfAPISuccess = 1.0f;
            LogHelper.printDebug(() -> "Resuming api calls");
        }
        return true;
    }

    /**
     * Injection point.
     *
     * @param originalUrl Image url for all litho images loaded, including video thumbnails.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            final boolean useDeArrow = shouldUseDeArrowAPI();
            final boolean useAltThumbnail = SettingsEnum.CLICKBAIT_ALT_THUMBNAIL.getBoolean();
            if (!useDeArrow && !useAltThumbnail) {
                return originalUrl;
            }
            if (!originalUrl.startsWith(YOUTUBE_THUMBNAIL_PREFIX)) return originalUrl;

            // Could use a regular expression here, but the format is simple enough to decode manually.
            final int videoIdStartIndex = originalUrl.indexOf('/', YOUTUBE_THUMBNAIL_PREFIX.length()) + 1;
            if (videoIdStartIndex <= 0) return originalUrl;
            final int videoIdEndIndex = originalUrl.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return originalUrl;
            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = originalUrl.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return originalUrl;
            int imageTypeEndIndex = originalUrl.indexOf('?', imageSizeEndIndex);
            if (imageTypeEndIndex < 0) imageTypeEndIndex = originalUrl.length();

            // Original URL but with user tracking information removed.
            String originalUrlSanitized = originalUrl.substring(0, imageTypeEndIndex);
            // Log the sanitized version, to keep user tracking details out of the logs.
            LogHelper.printDebug(() -> "Original url: " + originalUrlSanitized);
            String originalImageName = originalUrl.substring(imageSizeStartIndex, imageSizeEndIndex);

            final boolean useFastQuality = SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_FAST_QUALITY.getBoolean();
            final String alternateImagePrefix;
            switch (originalImageName) {
                case "maxresdefault":
                    // Not all videos have max or hq720 screen grabs.
                    // But it appears that nearly all videos have sd quality.
                    // Must use SD quality if skipping verification, otherwise some regular videos fail.
                    if (!useFastQuality) {
                        alternateImagePrefix = "maxres";
                        break;
                    } // else, fall thru to sd quality..
                case "hq720":
                    if (!useFastQuality) {
                        alternateImagePrefix = "hq720_";
                        break;
                    }
                case "sddefault":
                    alternateImagePrefix = "sd";
                    break;
                case "hqdefault":
                    alternateImagePrefix = "hq";
                    break;
                case "mqdefault":
                    alternateImagePrefix = "mq";
                    break;
                default:
                    return originalUrl; // Video is a short
            }

            String videoId = originalUrl.substring(videoIdStartIndex, videoIdEndIndex);
            String replacement = originalUrlSanitized;

            if (useAltThumbnail) {
                // Thumbnails can be upgraded to webp if they are not already,
                // but using the original format seems to load the fastest.
                String altThumbnailUrl = originalUrl.substring(0, videoIdStartIndex) + videoId + "/"
                        + alternateImagePrefix + SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.getInt()
                        + originalUrl.substring(imageSizeEndIndex, imageTypeEndIndex);
                if (verifyAltYouTubeThumbnailsExist(videoId, altThumbnailUrl)) {
                    replacement = altThumbnailUrl;
                } else if (!useDeArrow) {
                    return originalUrl;
                }
            }

            if (useDeArrow) {
                // For debugging and verifying the temporary off/on logic works.
                // Not a comprehensive check, and only forces loading an invalid video id with no redirect url.
                final  boolean FORCE_API_ERROR = false;
                //noinspection ConstantValue
                if (FORCE_API_ERROR && Math.random() < 0.7) {
                    videoId = "NON_EXISTENT_VIDEO_ID_DEBUG_PURPOSES";
                    replacement = "";
                }
                replacement = DEARROW_API_SERVER_ADDRESS + "/api/v1/getThumbnail"
                        + "?videoID=" + videoId
                        + "&redirectUrl=" + replacement;
            }

            final String replacementLog = replacement;
            LogHelper.printDebug(() -> "Replaced image with: " + replacementLog);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "DeArrow failure", ex);
            return originalUrl;
        }
    }

    private static void handleCronetCallback(UrlResponseInfo responseInfo, boolean successfulCall) {
        try {
            String url = responseInfo.getUrl();

            if (responseInfo.getHttpStatusCode() == 404
                    && SettingsEnum.CLICKBAIT_ALT_THUMBNAIL.getBoolean()
                    && url.startsWith(YOUTUBE_THUMBNAIL_PREFIX)) {
                // Fast alt thumbnails is enabled and the thumbnail is not available.
                // The video is:
                // - live stream
                // - upcoming unreleased video
                // - very old
                // - very low view count
                // Take note of this, so if the image reloads the original thumbnail will be used.
                final int videoIdStartIndex = url.indexOf('/', YOUTUBE_THUMBNAIL_PREFIX.length()) + 1;
                final int videoIdEndIndex = url.lastIndexOf('/');
                String videoId = url.substring(videoIdStartIndex, videoIdEndIndex);
                setVideoIdHasAltYouTubeThumbnails(videoId, false);
                return;
            }

            if (!SettingsEnum.CLICKBAIT_DEARROW.getBoolean() || !url.startsWith(DEARROW_API_SERVER_ADDRESS)) {
                return;
            }
            rollingAverageOfAPISuccess =
                    ((ROLLING_AVERAGE_NUMBER_OF_SAMPLES - 1) * rollingAverageOfAPISuccess + (successfulCall ? 1 : 0))
                            / ROLLING_AVERAGE_NUMBER_OF_SAMPLES;
            if (rollingAverageOfAPISuccess < 0.5f) {
                timeToResumeApiCalls = System.currentTimeMillis() + API_FAILURE_BACKOFF_TIME_MILLISECONDS;
                ReVancedUtils.showToastLong("DeArrow crowd source thumbnails temporarily not available");
            }
            LogHelper.printDebug(() -> {
                StringBuilder builder = new StringBuilder(successfulCall ? "handleCronetSucceeded" : "handleCronetFailure");
                builder.append(" response: ").append(responseInfo.getHttpStatusCode());
                if (rollingAverageOfAPISuccess != 1) {
                    builder.append(" averageSuccess: ").append(((int) (100 * rollingAverageOfAPISuccess) / 100.0f));
                }
                builder.append(" url: ").append(url);
                return builder.toString();
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "Cronet callback failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void handleCronetSucceeded(Object urlRequest, @NonNull UrlResponseInfo responseInfo) {
        handleCronetCallback(responseInfo, responseInfo.getHttpStatusCode() == 200);
    }

    /**
     * Injection point.
     */
    public static void handleCronetFailure(Object urlRequest, @Nullable UrlResponseInfo responseInfo, IOException exception) {
        if (responseInfo != null) handleCronetCallback(responseInfo, false);
    }
}
