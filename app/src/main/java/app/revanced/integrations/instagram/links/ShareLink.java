package app.revanced.integrations.instagram.links;

import app.revanced.integrations.shared.Logger;
import android.net.Uri;

public class ShareLink {

    public static String sanitizeUrl(String url){
        try{
            Uri uri = Uri.parse(url);
            //clear all the paramters and send back
            return uri.buildUpon().clearQuery().build().toString();

        }catch (Exception ex){
            Logger.printException(() -> "Instagram error", ex);
        }
        return url;
    }
}