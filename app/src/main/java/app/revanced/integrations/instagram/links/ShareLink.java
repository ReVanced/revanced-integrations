package app.revanced.integrations.instagram.links;

import app.revanced.integrations.shared.Logger;
import android.net.Uri;
import java.util.Arrays;
import java.util.List;

public class ShareLink {

    private static List trackerList;
    static {
        // Set of known tracker parameters.
        trackerList = Arrays.asList("igsh", "fbclid", "utm_source");
    }

    /*
     * This method used to remove unnecessary trackers from url.
     * The method takes in an url as string and returns a clean url as string.
     */
    public static String sanitizeUrl(String url){
        try{
            // Parse the URL.
            Uri uri = Uri.parse(url);

            // Build a clean url to append necessary parameters later.
            Uri.Builder uriBuilder = uri.buildUpon().clearQuery();

            // Iterate over the existing query parameters and re-add them except the one to remove.
            for (String key : uri.getQueryParameterNames()) {
                if (!trackerList.contains(key)) {
                    uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key));
                }
            }
            return uriBuilder.build().toString();

        }catch (Exception ex){
            Logger.printException(() -> "Instagram error", ex);
        }
        return url;
    }
}