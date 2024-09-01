package app.revanced.integrations.instagram.links;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import android.net.Uri;
import android.util.Log;
import android.content.Intent;
import android.content.Context;

public class ExternalBrowser {

    private static Uri removeTracker(String url) {
        // Parse the URL
        Uri uri = Uri.parse(url);

        // Build a clean url
        Uri.Builder uriBuilder = uri.buildUpon().clearQuery();

        // Iterate over the existing query parameters and re-add them except the one to remove
        for (String key : uri.getQueryParameterNames()) {
            if (!key.equals("fbclid")) {
                uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key));
            }
        }
        return uriBuilder.build();
    }

    public static boolean openInExternalBrowser(String url) {
        try{
            Uri uri = Uri.parse(url);
            //actual link is present in 'u' parameter
            String actualUrl = uri.getQueryParameter("u");

            //if there is no paramter as 'u' return false
            if(actualUrl == null) return false;

            //remove tracker
            Uri actualUri = removeTracker(actualUrl);

            //launch the link
            Context ctx = Utils.getContext();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, actualUri);
            ctx.startActivity(browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;

        }catch (Exception ex){
            Logger.printException(() -> "Instagram error", ex);
        }

        return false;
    }
}