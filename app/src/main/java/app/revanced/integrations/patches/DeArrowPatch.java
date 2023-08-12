package app.revanced.integrations.patches;

import java.io.IOException;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public final class DeArrowPatch {

    private static final boolean DEARROW_ENABLED = SettingsEnum.DEARROW_ENABLED.getBoolean();

    static {
        // Validate and correct any if user imported bad data.
        final int secondaryImageType = SettingsEnum.DEARROW_SECONDARY_IMAGE_TYPE.getInt();
        if (secondaryImageType <0 || secondaryImageType > 3) {
            LogHelper.printException(() -> "Invalid DeArrow secondary thumbnail image type: " + secondaryImageType);
            SettingsEnum.DEARROW_SECONDARY_IMAGE_TYPE.saveValue(SettingsEnum.DEARROW_SECONDARY_IMAGE_TYPE.defaultValue);
        }
    }

    public static String overrideImageURL(String originalUrl) {
        try {
            if (!DEARROW_ENABLED) return originalUrl;
            LogHelper.printDebug(() -> "Image url: " + originalUrl);

            String thumbnailPrefix = "https://i.ytimg.com/vi"; // '/vi/' or '/vi_webp/'
            if (!originalUrl.startsWith(thumbnailPrefix)) return originalUrl;

            final int videoIdStartIndex = originalUrl.indexOf('/', thumbnailPrefix.length()) + 1;
            if (videoIdStartIndex <= 0) return originalUrl;
            final int videoIdEndIndex = originalUrl.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return originalUrl;
            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = originalUrl.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return originalUrl;

            String originalImageSize = originalUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            final String secondaryImageSize;
            switch (originalImageSize) {
                case "maxresdefault":
                    // No in video thumbnails for this size.  Fall thru to next largest.
                    // Of note, the YouTube app/website does not seem to ever use the max res size.
                case "hq720":
                    secondaryImageSize = "hq720_";
                    break;
                case "sddefault":
                    secondaryImageSize = "sd";
                    break;
                case "hqdefault":
                    secondaryImageSize = "hq";
                    break;
                case "mqdefault":
                    secondaryImageSize = "mq";
                    break;
                default:
                    return originalUrl; // Thumbnail is a short or some unknown image type.
            }
            final String secondaryImageToUse;
            final int secondaryImageType = SettingsEnum.DEARROW_SECONDARY_IMAGE_TYPE.getInt();
            if (secondaryImageType == 0) {
                secondaryImageToUse = originalImageSize;
            } else {
                secondaryImageToUse = secondaryImageSize + secondaryImageType;
            }

            String videoId = originalUrl.substring(videoIdStartIndex, videoIdEndIndex);
            String replacement = "https://dearrow-thumb.ajay.app/api/v1/getThumbnail" + "?videoID=" + videoId
                    + "&redirectUrl=" + "https://i.ytimg.com/vi_webp/" + videoId + "/" + secondaryImageToUse + ".webp";
            LogHelper.printDebug(() -> "Replaced image with: " + replacement);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "DeArrow failure", ex);
            return originalUrl;
        }
    }

    public static void handleCronetFailure(Object request, Object responseInfo, IOException exception) {
        LogHelper.printDebug(() -> "handleCronetFailure request: " + request + " response: " + responseInfo + " exception:" + exception);
    }
}
