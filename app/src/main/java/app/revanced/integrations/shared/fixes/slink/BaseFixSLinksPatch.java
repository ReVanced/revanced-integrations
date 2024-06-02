package app.revanced.integrations.shared.fixes.slink;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Objects;

import static app.revanced.integrations.shared.Utils.getContext;


// This is a base class for to implement /s/ link resolution for 3rd party Reddit apps.
//
// Set webviewActivity as your app webview acitivity. This will be used for fallback if /s/
// link resolution fails.
//
// You need the following:
//    private FixSLinksPatch() {
//        this.webViewActivity = WebViewActivity.class;
//    }
//
// Your app would need to call this function before doing any of its own resolution
// Your app would also check the value of return result.
// If its true, app should *stop* all of its resolution and just sit there.
//
//    public static boolean patchResolveSLink(Context context, String link) {
//        return getInstance().resolve(context, link);
//    }
//
// Your app should call this at least once. Without this, /s/ link resolution would just do
// nothing. This function should be inserted at moment when app sets its own access_token
// You can look for following strings in app: bearer, access_token, Authorization.
//
//    public static void patchSetAccessToken(String access_token) {
//        getInstance().setAccessToken(access_token);
//    }
//
// This is required for both patchSetAccessToken and patchResolveSLink.
//
//    public static BaseFixSLinksPatch getInstance() {
//        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
//        return INSTANCE;
//    }
public abstract class BaseFixSLinksPatch {
    /**
     * The activity that will be used to open the link in a webview if the /s/ link resolution fails.
     */
    protected Class<? extends Activity> webViewActivity = null;

    /**
     * The access token that will be used to resolve the /s/ link.
     */
    protected String accessToken = null;

    /**
     * TODO: Document
     */
    protected String pendingUrl = null;

    /**
     * Singleton instance for patches.
     */
    protected static BaseFixSLinksPatch INSTANCE;

    public boolean resolveSLink(String link) {
        switch (resolveLink(link)) {
            case ACCESS_TOKEN_START: {
                pendingUrl = link;
                return true;
            }
            case DO_NOTHING:
                return true;
            default:
                return false;
        }
    }

    private ResolveResult resolveLink(String link) {
        Context context = getContext();
        if (link.matches(".*reddit\\.com/r/[^/]+/s/[^/]+")) {
            // A link ends with #bypass if it failed to resolve below.
            // resolveLink is called with the same link again but this time with #bypass
            // so that the link is opened in the app browser instead of trying to resolve it again.
            if (link.endsWith("#bypass")) {
                openInAppBrowser(context, link);

                return ResolveResult.DO_NOTHING;
            }

            Logger.printInfo(() -> "Resolving " + link);

            if (accessToken == null) {
                // This is not optimal.
                // However, an accessToken is necessary to make an authenticated request to Reddit.
                // in case Reddit has banned the IP - e.g. VPN.
                Intent startIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                context.startActivity(startIntent);

                return ResolveResult.ACCESS_TOKEN_START;
            }


            Utils.runOnBackgroundThread(() -> {
                String bypassLink = link + "#bypass";

                String finalLocation = bypassLink;
                try {
                    HttpURLConnection connection = getHttpURLConnection(link, accessToken);
                    connection.connect();
                    String location = connection.getHeaderField("location");
                    connection.disconnect();

                    // TODO: Check if this ends in java.lang.ExceptionInInitializerError.
                    Objects.requireNonNull(location, "Location is null");

                    finalLocation = location;
                    Logger.printInfo(() -> "Resolved " + link + " to " + location);
                } catch (SocketTimeoutException e) {
                    Logger.printException(() -> "Timeout when trying to resolve " + link, e);
                    finalLocation = bypassLink;
                } catch (Exception e) {
                    Logger.printException(() -> "Failed to resolve " + link, e);
                    finalLocation = bypassLink;
                } finally {
                    Intent startIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalLocation));
                    startIntent.setPackage(context.getPackageName());
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startIntent);
                }
            });

            return ResolveResult.DO_NOTHING;
        }

        return ResolveResult.CONTINUE;
    }

    public void setAccessToken(String accessToken) {
        Logger.printInfo(() -> "Setting access token");
        this.accessToken = accessToken;

        // In case a link was trying to be resolved before access token was set.
        // The link is resolved now, after the access token is set.
        if (pendingUrl != null) {
            String link = pendingUrl;
            pendingUrl = null;
            Logger.printInfo(() -> "Opening pending URL");
            resolveLink(link);
        }
    }

    private void openInAppBrowser(Context context, String link) {
        Intent intent = new Intent(context, webViewActivity);
        intent.putExtra("url", link);
        context.startActivity(intent);
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(String link, String accessToken) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (accessToken != null) {
            Logger.printInfo(() -> "Using access token for /s/ handling");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        } else {
            Logger.printInfo(() -> "Cant use saved access token for /s/ handling - its null");
        }
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("HEAD");
        // In case network has censors and blocks request to reddit we won't desire to become ANR
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        return connection;
    }
}