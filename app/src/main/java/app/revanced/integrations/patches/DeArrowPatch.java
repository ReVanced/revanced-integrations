package app.revanced.integrations.patches;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.StringTrieSearch;

public final class DeArrowPatch {

    private static final boolean DEARROW_ENABLED = SettingsEnum.DEARROW_ENABLED.getBoolean();

    private static boolean isValidThumbnailImageSize(@NonNull String imageSize) {
        String[] validThumbnailSizes = {"mqdefault", "sddefault", "hqdefault", "hq720", "maxresdefault"};
        for (String thumbnail : validThumbnailSizes) {
            if (thumbnail.equals(imageSize)) {
                return true;
            }
        }
        return false;
    }

    public static String overrideImageURL(String imageUrl) {
        try {
            if (!DEARROW_ENABLED) return imageUrl;
            LogHelper.printDebug(() -> "Image url: " + imageUrl);

            String thumbnailPrefix = "https://i.ytimg.com/vi"; // '/vi/' or '/vi_webp/'
            if (!imageUrl.startsWith(thumbnailPrefix)) return imageUrl;

            final int videoIdStartIndex = imageUrl.indexOf('/', thumbnailPrefix.length()) + 1;
            if (videoIdStartIndex <= 0) return imageUrl;
            final int videoIdEndIndex = imageUrl.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return imageUrl;
            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = imageUrl.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return imageUrl;

            String imageSize = imageUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            if (!isValidThumbnailImageSize(imageSize)) {
                return imageUrl; // a Short or other unknown image.
            }
            String videoId = imageUrl.substring(videoIdStartIndex, videoIdEndIndex);
            String replacement = "https://dearrow-thumb.ajay.app/api/v1/getThumbnail" + "?videoID=" + videoId
                    // If DeArrow is not available, then use webp of the original.
                    + "&redirectUrl=" + "https://i.ytimg.com/vi_webp/" + videoId + "/" + imageSize + ".webp";
            LogHelper.printDebug(() -> "Replaced image with: " + replacement);
            return replacement;
        } catch (Exception ex) {
            LogHelper.printException(() -> "DeArrow failure", ex);
            return imageUrl;
        }
    }

    public static void handleCronetFailure(Object request, Object responseInfo, IOException exception) {
        LogHelper.printDebug(() -> "handleCronetFailure request: " + request + " response: " + responseInfo + " exception:" + exception);
    }
}
