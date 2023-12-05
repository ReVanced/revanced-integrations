package app.revanced.integrations.patches;

import android.net.Uri;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.chromium.net.UrlResponseInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Alternative YouTube thumbnails.
 * <p>
 * Can show YouTube provided screen captures of beginning/middle/end of the video.
 * (ie: sd1.jpg, sd2.jpg, sd3.jpg).
 * <p>
 * Or can show crowd sourced thumbnails provided by DeArrow (http://dearrow.ajay.app).
 * <p>
 * Or can use DeArrow and fall back to screen captures if DeArrow is not available.
 * <p>
 * Has an additional option to use 'fast' video still thumbnails,
 * where it forces sd thumbnail quality and skips verifying if the alt thumbnail image exists.
 * The UI loading time will be the same or better than using the the original thumbnails,
 * but thumbnails will initially fail to load for all live streams, unreleased, and occasionally very old videos.
 * If a failed thumbnail load is reloaded (ie: scroll off, then on screen), then the original thumbnail
 * is reloaded instead.  Fast thumbnails requires using SD or lower thumbnail resolution,
 * because a noticeable number of videos do not have hq720 and too many fail to load.
 * <p>
 * Ideas for improvements:
 * - Selectively allow using original thumbnails in some situations,
 *   such as videos subscription feed, watch history, or in search results.
 * - Save to a temporary file the video id's verified to have alt thumbnails.
 *   This would speed up loading the watch history and users saved playlists.
 * @noinspection unused
 */
public final class AlternativeThumbnailsPatch {

    public static final int CONNECTION_TIMEOUT_MILLIS = 5000;

    /**
     * Single thread executor with the lowest priority.
     * Used simulate loading the original thumbnail with the view tracking parameters.
     * Only used when DeArrow is enabled as the tracking parameters are stripped from it's
     * redirect url parameter.
     */
    private static final ExecutorService viewTrackingExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    /**
     * Used to prevent sending view tracking parameters more than once.
     */
    @GuardedBy("itself")
    private static final Map<String, Boolean> videoIdsTrackingSent = new LinkedHashMap<>(100) {
        private static final int CACHE_LIMIT = 1000;
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > CACHE_LIMIT; // Evict the oldest entry if over the cache limit.
        }
    };

    private static final Uri dearrowApiUri;
    /**
     * The scheme and host of {@link #dearrowApiUri}.
     */
    private static final String deArrowApiUrlPrefix;

    /**
     * How long to temporarily turn off DeArrow if it fails for any reason.
     */
    private static final long DEARROW_CONNECTION_FAILURE_BACKOFF_MILLISECONDS = 2 * 60 * 1000; // 2 Minutes.

    /**
     * If non zero, then the system time of when DeArrow API calls can resume.
     */
    private static volatile long timeToResumeDeArrowAPICalls;

    static {
        dearrowApiUri = validateSettings();
        deArrowApiUrlPrefix = dearrowApiUri.getScheme() + "://" + dearrowApiUri.getHost() + "/";
    }

    /**
     * Fix any bad imported data.
     */
    private static Uri validateSettings() {
        final int mode = SettingsEnum.ALT_THUMBNAIL_MODE.getInt();
        if (mode < 1 || mode > 4) {
            ReVancedUtils.showToastLong("Invalid Alternative thumbnail mode: "
                    + mode + ".  Using default");
            SettingsEnum.ALT_THUMBNAIL_MODE.resetToDefault();
        }

        final int altThumbnailType = SettingsEnum.ALT_THUMBNAIL_STILL_TIME.getInt();
        if (altThumbnailType < 1 || altThumbnailType > 3) {
            ReVancedUtils.showToastLong("Invalid Alternative still thumbnail type: "
                    + altThumbnailType + ". Using default");
            SettingsEnum.ALT_THUMBNAIL_STILL_TIME.resetToDefault();
        }

        Uri apiUri = Uri.parse(SettingsEnum.ALT_THUMBNAIL_DEARROW_API_URL.getString());
        if (apiUri.getScheme() == null || apiUri.getHost() == null) {
            ReVancedUtils.showToastLong("Invalid DeArrow API URL. Using default");
            SettingsEnum.ALT_THUMBNAIL_DEARROW_API_URL.resetToDefault();
            return validateSettings();
        }
        return apiUri;
    }

    /**
     * Build the alternative thumbnail url using video stills from the beginning / middle / end thumbnails.
     *
     * @param decodedUrl Decoded original thumbnail request url.
     * @return The alternative thumbnail url, or the original url. Both without tracking parameters.
     */
    @NonNull
    private static String buildYoutubeVideoStillURL(DecodedThumbnailUrl decodedUrl) {
        ThumbnailQuality qualityToUse = ThumbnailQuality.getQualityToUse(decodedUrl.imageQuality);
        if (qualityToUse == null) return decodedUrl.sanitizedUrl; // Video is a short.

        String sanitizedReplacement = decodedUrl.createStillUrl(qualityToUse.getAltImageNameToUse(), false);
        if (VerifiedQualities.verifyAltThumbnailExist(decodedUrl.videoId, qualityToUse, sanitizedReplacement)) {
            return sanitizedReplacement;
        }
        return decodedUrl.sanitizedUrl;
    }

    /**
     * Build the alternative thumbnail url using DeArrow thumbnail cache.
     *
     * @param videoId ID of the video to get a thumbnail of.
     * @param fallbackUrl URL to fall back to in case.
     * @return The alternative thumbnail url, without tracking parameters.
     */
    @NonNull
    private static String buildDeArrowThumbnailURL(String videoId, String fallbackUrl) {
        // Build thumbnail request url.
        // See https://github.com/ajayyy/DeArrowThumbnailCache/blob/29eb4359ebdf823626c79d944a901492d760bbbc/app.py#L29.
        return dearrowApiUri
                .buildUpon()
                .appendQueryParameter("videoID", videoId)
                .appendQueryParameter("officialTime", "true")
                .appendQueryParameter("redirectUrl", fallbackUrl)
                .build()
                .toString();
    }

    private static boolean urlIsDeArrow(@NonNull String imageUrl) {
        return imageUrl.startsWith(deArrowApiUrlPrefix);
    }

    /**
     * @return If this client has not recently experience any DeArrow API errors.
     */
    private static boolean canUseDeArrowAPI() {
        if (timeToResumeDeArrowAPICalls == 0) {
            return true;
        }
        if (System.currentTimeMillis() > timeToResumeDeArrowAPICalls) {
            LogHelper.printDebug(() -> "Resuming DeArrow API calls");
            timeToResumeDeArrowAPICalls = 0;
            return true;
        }
        return false;
    }

    private static void handleDeArrowError(UrlResponseInfo responseInfo) {
        // TODO? Add a setting to show a toast on DeArrow failure?
        LogHelper.printDebug(() -> "Encountered DeArrow error.  Backing off for "
                        + DEARROW_CONNECTION_FAILURE_BACKOFF_MILLISECONDS + "ms: " + responseInfo);
        timeToResumeDeArrowAPICalls = System.currentTimeMillis() + DEARROW_CONNECTION_FAILURE_BACKOFF_MILLISECONDS;
    }

    /**
     * Because the view tracking parameters are not included and likely are used for recommendations,
     * make a dummy call in the background for the original thumbnail and include the tracking parameters.
     *
     * This may do nothing if YouTube does not detect the
     */
    private static void makeDummyViewTrackingCall(@NonNull DecodedThumbnailUrl decodedUrl) {
        if (decodedUrl.viewTrackingParameters.isEmpty()) {
            return; // Nothing to do.
        }
        if (videoIdsTrackingSent.put(decodedUrl.videoId, Boolean.TRUE) != null) {
            return; // Already sent tracking for this video.
        }
        viewTrackingExecutor.execute(() -> {
            try {
                String qualityToUse = ThumbnailQuality.DEFAULT.originalName; // Use the lowest quality.
                String lowQualityWithTracking = decodedUrl.createStillUrl(qualityToUse, true);
                HttpURLConnection connection = (HttpURLConnection) new URL(lowQualityWithTracking).openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
                connection.setReadTimeout(CONNECTION_TIMEOUT_MILLIS);
                final int responseCode = connection.getResponseCode();
                LogHelper.printDebug(() -> "Finished sending viewing parameters for video: "
                        + decodedUrl.videoId + " with response: " + responseCode);
            } catch (Exception ex) {
                LogHelper.printInfo(() -> "View tracking failure", ex);
            }
        });
    }

    /**
     * Injection point.  Called off the main thread and by multiple threads at the same time.
     *
     * @param originalUrl Image url for all url images loaded, including video thumbnails.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            final var thumbnailMode = AlternativeThumbnailMode.getCurrent();
            if (thumbnailMode == AlternativeThumbnailMode.ORIGINAL) {
                return originalUrl;
            }

            final var decodedUrl = DecodedThumbnailUrl.decodeImageUrl(originalUrl);
            if (decodedUrl == null) {
                return originalUrl; // Not a thumbnail.
            }

            // Keep any tracking parameters out of the logs, and log only the base URL.
            LogHelper.printDebug(() -> "Original url: " + decodedUrl.sanitizedUrl);

            final StringBuilder thumbnailUrlBuilder = new StringBuilder();
            final String sanitizedReplacementUrl;
            final boolean includeTrackingParameters;
            if (thumbnailMode.usingDeArrow() && canUseDeArrowAPI()) {
                // Get fallback URL.
                final String fallbackUrl = thumbnailMode == AlternativeThumbnailMode.DEARROW_OR_VIDEO_STILLS
                        ? buildYoutubeVideoStillURL(decodedUrl)
                        : decodedUrl.sanitizedUrl;

                makeDummyViewTrackingCall(decodedUrl);

                sanitizedReplacementUrl = buildDeArrowThumbnailURL(decodedUrl.videoId, fallbackUrl);
                includeTrackingParameters = false;
            } else if (thumbnailMode.usingVideoStills()) {
                // Get video still URL.
                sanitizedReplacementUrl = buildYoutubeVideoStillURL(decodedUrl);
                includeTrackingParameters = true;
            } else {
                return originalUrl; // Recently experienced DeArrow failure and video stills are not enabled.
            }

            thumbnailUrlBuilder.append(sanitizedReplacementUrl);
            if (includeTrackingParameters) {
                // View tracking parameters are presumably used to determine if a user has viewed a thumbnail.
                // This likely is used for recommendations, so they are retained if present.
                thumbnailUrlBuilder.append(decodedUrl.viewTrackingParameters);
            }

            // Do not log the tracking parameters.
            LogHelper.printDebug(() -> "Replacement url: " + sanitizedReplacementUrl);

            return sanitizedReplacementUrl;
        } catch (Exception ex) {
            LogHelper.printException(() -> "overrideImageURL failure", ex);
            return originalUrl;
        }
    }

    /**
     * Injection point.
     * <p>
     * Cronet considers all completed connections as a success, even if the response is 404 or 5xx.
     */
    public static void handleCronetSuccess(@NonNull UrlResponseInfo responseInfo) {
        try {
            final int responseCode = responseInfo.getHttpStatusCode();
            if (responseCode != 200) {
                AlternativeThumbnailMode currentMode = AlternativeThumbnailMode.getCurrent();
                String url = responseInfo.getUrl();
                // Do not log the responseInfo unless it's found to be a DeArrow call.
                // Otherwise this can log user details found in regular YouTube non Alt Thumbnails traffic.
                LogHelper.printDebug(() -> "handleCronetSuccess responseCode: " + responseCode + " url: " + url);

                if (currentMode.usingDeArrow() && urlIsDeArrow(url)) {
                    handleDeArrowError(responseInfo);
                }

                if (currentMode.usingVideoStills() && responseCode == 404) {
                    // Fast alt thumbnails is enabled and the thumbnail is not available.
                    // The video is:
                    // - live stream
                    // - upcoming unreleased video
                    // - very old
                    // - very low view count
                    // Take note of this, so if the image reloads the original thumbnail will be used.
                    DecodedThumbnailUrl decodedUrl = DecodedThumbnailUrl.decodeImageUrl(url);
                    if (decodedUrl == null) {
                        return; // Not a thumbnail.
                    }

                    ThumbnailQuality quality = ThumbnailQuality.altImageNameToQuality(decodedUrl.imageQuality);
                    if (quality == null) {
                        // Video is a short or unknown quality, but the url returned 404.  Should never happen.
                        LogHelper.printDebug(() -> "Failed to load unknown url: " + decodedUrl.sanitizedUrl);
                        return;
                    }

                    VerifiedQualities.setAltThumbnailDoesNotExist(decodedUrl.videoId, quality);
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Callback success error", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void handleCronetFailure(@Nullable UrlResponseInfo responseInfo, IOException exception) {
        try {
            LogHelper.printDebug(() -> "handleCronetFailure exception: " + exception);
            AlternativeThumbnailMode currentMode = AlternativeThumbnailMode.getCurrent();

            if (currentMode.usingDeArrow()) {
                // If the DeArrow API host name does not resolve, then no response is provided
                // and the IOException (CronetException) provides no information to detect this situation.
                //
                // For now, treat this as a DeArrow failure but only if the API is not set to default.
                // This may incorrectly turn off DeArrow for non alt thumbnail errors,
                // but that should be rare since so few users will change the API url.
                if ((responseInfo == null && !SettingsEnum.ALT_THUMBNAIL_DEARROW_API_URL.isSetToDefault())
                        || (responseInfo != null && urlIsDeArrow(responseInfo.getUrl()))) {
                    handleDeArrowError(responseInfo);
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Callback failure error", ex);
        }
    }

    private enum ThumbnailQuality {
        // In order of lowest to highest resolution.
        DEFAULT("default", ""), // effective alt name is 1.jpg, 2.jpg, 3.jpg
        MQDEFAULT("mqdefault", "mq"),
        HQDEFAULT("hqdefault", "hq"),
        SDDEFAULT("sddefault", "sd"),
        HQ720("hq720", "hq720_"),
        MAXRESDEFAULT("maxresdefault", "maxres");

        /**
         * Lookup map of original name to enum.
         */
        private static final Map<String, ThumbnailQuality> originalNameToEnum = new HashMap<>();

        /**
         * Lookup map of alt name to enum.  ie: "hq720_1" to {@link #HQ720}.
         */
        private static final Map<String, ThumbnailQuality> altNameToEnum = new HashMap<>();

        static {
            for (ThumbnailQuality quality : values()) {
                originalNameToEnum.put(quality.originalName, quality);

                for (int i = 1; i <= 3; i++) {
                    altNameToEnum.put(quality.altImageName + i, quality);
                }
            }
        }

        /**
         * Convert an alt image name to enum.
         * ie: "hq720_2" returns {@link #HQ720}.
         */
        @Nullable
        static ThumbnailQuality altImageNameToQuality(@NonNull String altImageName) {
            return altNameToEnum.get(altImageName);
        }

        /**
         * Original quality to effective alt quality to use.
         * ie: If fast alt image is enabled, then "hq720" returns {@link #SDDEFAULT}.
         */
        @Nullable
        static ThumbnailQuality getQualityToUse(@NonNull String originalSize) {
            ThumbnailQuality quality = originalNameToEnum.get(originalSize);
            if (quality == null) {
                return null; // Not a thumbnail for a regular video.
            }

            final boolean useFastQuality = SettingsEnum.ALT_THUMBNAIL_STILL_FAST_QUALITY.getBoolean();
            switch (quality) {
                case SDDEFAULT:
                    // SD alt images have somewhat worse quality with washed out color and poor contrast.
                    // But the 720 images look much better and don't suffer from these issues.
                    // For unknown reasons, the 720 thumbnails are used only for the home feed,
                    // while SD is used for the search and subscription feed
                    // (even though search and subscriptions use the exact same layout as the home feed).
                    // Of note, this image quality issue only appears with the alt thumbnail images,
                    // and the regular thumbnails have identical color/contrast quality for all sizes.
                    // Fix this by falling thru and upgrading SD to 720.
                case HQ720:
                    if (useFastQuality) {
                        return SDDEFAULT; // SD is max resolution for fast alt images.
                    }
                    return HQ720;
                case MAXRESDEFAULT:
                    if (useFastQuality) {
                        return SDDEFAULT;
                    }
                    return MAXRESDEFAULT;
                default:
                    return quality;
            }
        }

        final String originalName;
        final String altImageName;

        ThumbnailQuality(String originalName, String altImageName) {
            this.originalName = originalName;
            this.altImageName = altImageName;
        }

        String getAltImageNameToUse() {
            return altImageName + SettingsEnum.ALT_THUMBNAIL_STILL_TIME.getInt();
        }
    }

    /**
     * Uses HTTP HEAD requests to verify and keep track of which thumbnail sizes
     * are available and not available.
     */
    private static class VerifiedQualities {
        /**
         * After a quality is verified as not available, how long until the quality is re-verified again.
         * Used only if fast mode is not enabled. Intended for live streams and unreleased videos
         * that are now finished and available (and thus, the alt thumbnails are also now available).
         */
        private static final long NOT_AVAILABLE_TIMEOUT_MILLISECONDS = 10 * 60 * 1000; // 10 minutes.

        /**
         * Cache used to verify if an alternative thumbnails exists for a given video id.
         */
        @GuardedBy("itself")
        private static final Map<String, VerifiedQualities> altVideoIdLookup = new LinkedHashMap<>(100) {
            private static final int CACHE_LIMIT = 1000;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_LIMIT; // Evict the oldest entry if over the cache limit.
            }
        };

        private static VerifiedQualities getVerifiedQualities(@NonNull String videoId, boolean returnNullIfDoesNotExist) {
            synchronized (altVideoIdLookup) {
                VerifiedQualities verified = altVideoIdLookup.get(videoId);
                if (verified == null) {
                    if (returnNullIfDoesNotExist) {
                        return null;
                    }
                    verified = new VerifiedQualities();
                    altVideoIdLookup.put(videoId, verified);
                }
                return verified;
            }
        }

        static boolean verifyAltThumbnailExist(@NonNull String videoId, @NonNull ThumbnailQuality quality,
                                               @NonNull String imageUrl) {
            VerifiedQualities verified = getVerifiedQualities(videoId, SettingsEnum.ALT_THUMBNAIL_STILL_FAST_QUALITY.getBoolean());
            if (verified == null) return true; // Fast alt thumbnails is enabled.
            return verified.verifyYouTubeThumbnailExists(videoId, quality, imageUrl);
        }

        static void setAltThumbnailDoesNotExist(@NonNull String videoId, @NonNull ThumbnailQuality quality) {
            VerifiedQualities verified = getVerifiedQualities(videoId, false);
            if (verified == null) return;
            verified.setQualityVerified(videoId, quality, false);
        }

        /**
         * Highest quality verified as existing.
         */
        @Nullable
        ThumbnailQuality highestQualityVerified;
        /**
         * Lowest quality verified as not existing.
         */
        @Nullable
        ThumbnailQuality lowestQualityNotAvailable;

        /**
         * System time, of when to invalidate {@link #lowestQualityNotAvailable}.
         * Used only if fast mode is not enabled.
         */
        long timeToReVerifyLowestQuality;

        synchronized void setQualityVerified(String videoId, ThumbnailQuality quality, boolean isVerified) {
            if (isVerified) {
                if (highestQualityVerified == null || highestQualityVerified.ordinal() < quality.ordinal()) {
                    highestQualityVerified = quality;
                }
            } else {
                if (lowestQualityNotAvailable == null || lowestQualityNotAvailable.ordinal() > quality.ordinal()) {
                    lowestQualityNotAvailable = quality;
                    timeToReVerifyLowestQuality = System.currentTimeMillis() + NOT_AVAILABLE_TIMEOUT_MILLISECONDS;
                }
                LogHelper.printDebug(() -> quality + " not available for video: " + videoId);
            }
        }

        /**
         * Verify if a video alt thumbnail exists.  Does so by making a minimal HEAD http request.
         */
        synchronized boolean verifyYouTubeThumbnailExists(@NonNull String videoId, @NonNull ThumbnailQuality quality,
                                                          @NonNull String imageUrl) {
            if (highestQualityVerified != null && highestQualityVerified.ordinal() >= quality.ordinal()) {
                return true; // Previously verified as existing.
            }

            final boolean fastQuality = SettingsEnum.ALT_THUMBNAIL_STILL_FAST_QUALITY.getBoolean();
            if (lowestQualityNotAvailable != null && lowestQualityNotAvailable.ordinal() <= quality.ordinal()) {
                if (fastQuality || System.currentTimeMillis() < timeToReVerifyLowestQuality) {
                    return false; // Previously verified as not existing.
                }
                // Enough time has passed, and should re-verify again.
                LogHelper.printDebug(() -> "Resetting lowest verified quality for: " + videoId);
                lowestQualityNotAvailable = null;
            }

            if (fastQuality) {
                return true; // Unknown if it exists or not.  Use the URL anyways and update afterwards if loading fails.
            }

            boolean imageFileFound;
            try {
                LogHelper.printDebug(() -> "Verifying image: " + imageUrl);
                // This hooked code is running on a low priority thread, and it's slightly faster
                // to run the url connection thru the integrations thread pool which runs at the highest priority.
                final long start = System.currentTimeMillis();
                imageFileFound = ReVancedUtils.submitOnBackgroundThread(() -> {
                    HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
                    connection.setReadTimeout(CONNECTION_TIMEOUT_MILLIS);
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
                imageFileFound = false;
            }

            setQualityVerified(videoId, quality, imageFileFound);
            return imageFileFound;
        }
    }

    /**
     * YouTube video thumbnail url, decoded into it's relevant parts.
     */
    private static class DecodedThumbnailUrl {
        /**
         * YouTube thumbnail URL prefix. Can be '/vi/' or '/vi_webp/'
         */
        private static final String YOUTUBE_THUMBNAIL_PREFIX = "https://i.ytimg.com/vi";

        @Nullable
        static DecodedThumbnailUrl decodeImageUrl(String url) {
            final int videoIdStartIndex = url.indexOf('/', YOUTUBE_THUMBNAIL_PREFIX.length()) + 1;
            if (videoIdStartIndex <= 0) return null;

            final int videoIdEndIndex = url.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return null;

            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = url.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return null;

            int imageExtensionEndIndex = url.indexOf('?', imageSizeEndIndex);
            if (imageExtensionEndIndex < 0) imageExtensionEndIndex = url.length();

            return new DecodedThumbnailUrl(url, videoIdStartIndex, videoIdEndIndex,
                    imageSizeStartIndex, imageSizeEndIndex, imageExtensionEndIndex);
        }

        final String originalFullUrl;
        /** Full usable url, but stripped of any tracking information. */
        final String sanitizedUrl;
        /** Url up to the video ID. */
        final String urlPrefix;
        final String videoId;
        /** Quality, such as hq720 or sddefault. */
        final String imageQuality;
        /** JPG or WEBP */
        final String imageExtension;
        /** User view tracking parameters, only present on some images. */
        final String viewTrackingParameters;

        private DecodedThumbnailUrl(String fullUrl, int videoIdStartIndex, int videoIdEndIndex,
                                    int imageSizeStartIndex, int imageSizeEndIndex, int imageExtensionEndIndex) {
            originalFullUrl = fullUrl;
            sanitizedUrl = fullUrl.substring(0, imageExtensionEndIndex);
            urlPrefix = fullUrl.substring(0, videoIdStartIndex);
            videoId = fullUrl.substring(videoIdStartIndex, videoIdEndIndex);
            imageQuality = fullUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            imageExtension = fullUrl.substring(imageSizeEndIndex + 1, imageExtensionEndIndex);
            viewTrackingParameters = (imageExtensionEndIndex == fullUrl.length())
                    ? "" : fullUrl.substring(imageExtensionEndIndex);
        }

        String createStillUrl(@NonNull String imageQualityName, boolean includeViewTracking) {
            // Images could be upgraded to webp if they are not already, but this fails quite often,
            // especially for new videos uploaded in the last hour.
            // And even if alt webp images do exist, sometimes they can load much slower than the original jpg alt images.
            // (as much as 4x slower has been observed, despite the alt webp image being a smaller file).
            StringBuilder builder = new StringBuilder(originalFullUrl.length() + 2);
            builder.append(urlPrefix);
            builder.append(videoId).append('/');
            builder.append(imageQualityName);
            builder.append('.').append(imageExtension);
            if (includeViewTracking) {
                builder.append(viewTrackingParameters);
            }
            return builder.toString();
        }
    }

    /**
     * Alternative thumbnail mode.
     */
    private enum AlternativeThumbnailMode {
        /**
         * Use the original thumbnails provided by the content creator.
         * This effectively disables the patch, as this options matches the stock behaviour.
         */
        ORIGINAL(1),

        /**
         * Use video stills provided by YouTube.
         * Uses {@link SettingsEnum#ALT_THUMBNAIL_STILL_TIME} and {@link SettingsEnum#ALT_THUMBNAIL_STILL_FAST_QUALITY}.
         */
        VIDEO_STILLS(2),

        /**
         * Use thumbnails provided by DeArrow, fallback to {@link AlternativeThumbnailMode#ORIGINAL}.
         */
        DEARROW_OR_CREATOR_PROVIDED(3),

        /**
         * Use thumbnails provided by DeArrow, fallback to {@link AlternativeThumbnailMode#VIDEO_STILLS}.
         */
        DEARROW_OR_VIDEO_STILLS(4);

        private final int id;
        AlternativeThumbnailMode(int id) {
            this.id = id;
        }

        public boolean usingVideoStills() {
            return this == VIDEO_STILLS || this == DEARROW_OR_VIDEO_STILLS;
        }

        public boolean usingDeArrow() {
            return this == DEARROW_OR_CREATOR_PROVIDED || this == DEARROW_OR_VIDEO_STILLS;
        }

        @NonNull
        public static AlternativeThumbnailMode byId(int id) {
            // Could use the Enum ordinal and use values()[id],
            // but then the ids would start at 0.
            // Since only 4 ids exist this isn't a big deal
            // and little overhead is needed to manually compare 4 int values.
            for (final var mode : AlternativeThumbnailMode.values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            // User imported bad data and did not restart the app. Fix the settings and continue.
            validateSettings();
            return byId(id);
        }

        @NonNull
        public static AlternativeThumbnailMode getCurrent() {
            return byId(SettingsEnum.ALT_THUMBNAIL_MODE.getInt());
        }
    }
}
