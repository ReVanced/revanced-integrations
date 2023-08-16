package app.revanced.integrations.patches;

import androidx.annotation.NonNull;

import org.chromium.net.UrlResponseInfo;

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
 * Alternate YouTube provided thumbnails, showing the beginning/middle/end of the video.
 * (ie: sd1.jpg, sd2.jpg, sd3.jpg).
 *
 * Has an additional option to use 'fast' thumbnails,
 * where it forces sd thumbnail quality and skips verifying if the alt thumbnail image exists.
 * The UI loading time will be the same or better than using the the original thumbnails,
 * but thumbnails will initially fail to load for all live streams, unreleased, and occasionally very old videos.
 * If a failed thumbnail load is reloaded (ie: scroll off, then on screen), then the original thumbnail
 * is reloaded instead.  Fast thumbnails requires using SD or lower thumbnail resolution,
 * because a noticeable number of videos do not have hq720 and they fail to load.
 *
 * Ideas for improvements:
 * - Selectively allow using original thumbnails in some situations,
 *   such as videos subscription feed, watch history, or in search results.
 * - Save to a temporary file, the video id's verified to have alt thumbnails.
 *   This would speed up loading the watch history and users saved playlists.
 */
public final class AlternateThumbnailsPatch {

    /**
     * YouTube thumbnail URL prefix. Can be '/vi/' or '/vi_webp/'
     */
    public static final String YOUTUBE_THUMBNAIL_PREFIX = "https://i.ytimg.com/vi";

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
        final int altThumbnailType = SettingsEnum.ALT_THUMBNAIL_TYPE.getInt();
        if (altThumbnailType < 1 || altThumbnailType > 3) {
            LogHelper.printException(() -> "Invalid alt thumbnail type: " + altThumbnailType);
            SettingsEnum.ALT_THUMBNAIL_TYPE.saveValue(SettingsEnum.ALT_THUMBNAIL_TYPE.defaultValue);
        }
    }

    private static void setVideoIdHasAltYouTubeThumbnails(@NonNull String videoId, boolean isValid) {
        altVideoIdLookup.put(Objects.requireNonNull(videoId), isValid);
        if (!isValid) {
            LogHelper.printDebug(() -> "Alt thumbnails not available for video: " + videoId);
        }
    }

    /**
     * Verify if a video alt thumbnail exists.  Does so by making a minimal HEAD http request.
     *
     *
     * Known limitation of this implementation:
     *
     * This only checks if alt images exist for the requested resolution,
     * and will fail to detect if the video only has low resolution alt images (ie: hq720_2 does not exist, but sd2 does).
     * This could try checking for lower resolution images until it finds an alt image that exists.
     * But that would make loading slower, especially for live streams or other videos that have zero alt thumbnails.
     *
     * The videos that do not have high resolution alt images are almost always very old (10+ years old),
     * or it's an obscure video with a very low view count. So for now, ignore these videos and show the original thumbnail.
     */
    private static boolean verifyAltYouTubeThumbnailsExist(@NonNull String videoId, @NonNull String imageUrl) {
        LogHelper.printDebug(() -> "Alt image: " + imageUrl);

        Boolean hasAltThumbnails = altVideoIdLookup.get(videoId);
        if (hasAltThumbnails == null) {
            if (SettingsEnum.ALT_THUMBNAIL_FAST_QUALITY.getBoolean()) {
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
     * Injection point.
     *
     * @param originalUrl Image url for all url images loaded, including video thumbnails.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            if (!SettingsEnum.ALT_THUMBNAIL.getBoolean()) {
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

            // Keep any tracking parameters out of the logs, and log only the base url.
            final int imageTypeEndIndexLogging = imageTypeEndIndex;
            LogHelper.printDebug(() -> "Original url: " + originalUrl.substring(0, imageTypeEndIndexLogging));
            String originalImageName = originalUrl.substring(imageSizeStartIndex, imageSizeEndIndex);

            final boolean useFastQuality = SettingsEnum.ALT_THUMBNAIL_FAST_QUALITY.getBoolean();
            final String alternateImagePrefix;
            switch (originalImageName) {
                case "maxresdefault":
                    if (!useFastQuality) {
                        alternateImagePrefix = "maxres";
                        break;
                    } // else, fall thru to sd quality.
                case "hq720":
                    if (!useFastQuality) {
                        alternateImagePrefix = "hq720_";
                        break;
                    }
                case "sddefault":
                    if (!useFastQuality) {
                        // SD alt images have somewhat worse quality with washed out color and poor contrast.
                        // But the 720 images look much better and don't suffer from these issues.
                        // For unknown reasons, the 720 thumbnails are used only for the home feed,
                        // while SD is used for the search and subscription feed
                        // (even though search and subscriptions uses the exact same layout as the home feed).
                        // Of note, this image quality issue only appears with the alt thumbnail images,
                        // and the regular thumbnails have identical color/contrast quality for all sizes.
                        //
                        // To improve this situation, upgrade to 720 if SD is requested.
                        alternateImagePrefix = "hq720_";
                        break;
                    }
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

            // Images could be upgraded to webp if they are not already, but this fails quite often,
            // especially for new videos uploaded in the last hour.
            // And even if alt webp images do exist, sometimes they can load much slower than the original jpg alt images.
            // (as much as 4x slower has been observed, despite the alt webp image being a smaller file).

            String videoId = originalUrl.substring(videoIdStartIndex, videoIdEndIndex);

            StringBuilder builder = new StringBuilder();
            builder.append(originalUrl, 0, videoIdStartIndex);
            builder.append(videoId).append('/');
            builder.append(alternateImagePrefix).append(SettingsEnum.ALT_THUMBNAIL_TYPE.getInt());
            builder.append(originalUrl, imageSizeEndIndex, imageTypeEndIndex);

            if (!verifyAltYouTubeThumbnailsExist(videoId, builder.toString())) {
                return originalUrl;
            }

            // URL tracking parameters. Presumably they are to determine if a user has viewed a thumbnail.
            // This likely is used for recommendations, so they are retained if present.
            builder.append(originalUrl, imageTypeEndIndex, originalUrl.length());
            return builder.toString();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Alt thumbnails failure", ex);
            return originalUrl;
        }
    }

    /**
     * Injection point.
     *
     * Cronet considers a completed connection as a success, even if the response is 404 or 5xx.
     */
    public static void handleCronetSuccess(@NonNull UrlResponseInfo responseInfo) {
        try {
            String url = responseInfo.getUrl();

            if (responseInfo.getHttpStatusCode() == 404
                    && SettingsEnum.ALT_THUMBNAIL.getBoolean()
                    && url.startsWith(YOUTUBE_THUMBNAIL_PREFIX)) {
                // Fast alt thumbnails is enabled and the thumbnail is not available.
                // The video is:
                // - live stream
                // - upcoming unreleased video
                // - very old
                // - very low view count
                // Take note of this, so if the image reloads the original thumbnail will be used.
                final int videoIdStartIndex = url.indexOf('/', YOUTUBE_THUMBNAIL_PREFIX.length()) + 1;
                final int videoIdEndIndex = url.indexOf('/', videoIdStartIndex);
                String videoId = url.substring(videoIdStartIndex, videoIdEndIndex);
                setVideoIdHasAltYouTubeThumbnails(videoId, false);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Alt thumbnails callback failure", ex);
        }
    }

}
