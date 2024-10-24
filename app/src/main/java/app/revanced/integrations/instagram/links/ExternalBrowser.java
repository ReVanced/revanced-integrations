package app.revanced.integrations.instagram.links;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import android.net.Uri;
import android.content.Intent;
import android.content.Context;

public class ExternalBrowser {

    /*
     * This method used to launch an url in external browser.
     * The method takes in an url as string and returns true if browser is launched and false otherwise
     */
    public static boolean openInExternalBrowser(String url) {
        try{
            Uri uri = Uri.parse(url);
            // Actual link is present in 'u' parameter.
            String actualUrl = uri.getQueryParameter("u");

            // If there is no paramter as 'u' return false.
            if(actualUrl == null) return false;

            // Remove unneccessary trackers from the url.
            String cleanUrl = ShareLink.sanitizeUrl(actualUrl);

            // Launch the url.
            Context ctx = Utils.getContext();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl));
            ctx.startActivity(browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;

        }catch (Exception ex){
            Logger.printException(() -> "Instagram error", ex);
        }

        return false;
    }
}