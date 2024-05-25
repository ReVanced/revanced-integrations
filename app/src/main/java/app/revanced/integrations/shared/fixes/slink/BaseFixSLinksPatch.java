package app.revanced.integrations.shared.fixes.slink;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import static app.revanced.integrations.shared.Utils.getContext;

public abstract class BaseFixSLinksPatch {
    protected Class<? extends Activity> webViewActivity = null;
    String accessToken = null;
    public String pendingUrl = null;
    protected static BaseFixSLinksPatch INSTANCE;

    public boolean resolve(Context context, String link) {
        ResolveResult res = performResolution(context, link);
        boolean ret = false;
        switch (res) {
            case ACCESS_TOKEN_START: {
                pendingUrl = link;
                ret = true;
                break;
            }
            case DO_NOTHING:
                ret = true;
                break;
            default:
                break;
        }
        return ret;
    }

    public ResolveResult performResolution(Context context, String link) {
        if (link.matches(".*reddit\\.com/r/[^/]+/s/[^/]+")) {
            Logger.printInfo(() -> "Resolving " + link);
            if (link.endsWith("#bypass")) {
                openInAppBrowser(context, link);
                return ResolveResult.DO_NOTHING;
            }
            String accessToken = getUserAccessToken();
            if (accessToken == null) {
                // This is not optimal.
                // However, we need to get access_token to properly auth request, especially if user
                // has banned IP - e.g. VPN.
                Intent startIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                context.startActivity(startIntent);
                return ResolveResult.ACCESS_TOKEN_START;
            }
            String bypassLink = link + "#bypass";
            Utils.runOnBackgroundThread(() -> {
                String finalLocation = bypassLink;
                try {
                    HttpURLConnection connection = getHttpURLConnection(link, accessToken);
                    connection.connect();
                    String location = connection.getHeaderField("location");
                    connection.disconnect();

                    // For some reason using requireNotNull or similar ends up in java.lang.ExceptionInInitializerError,
                    // despite exception being caught down below?
                    if (location == null) {
                        Logger.printInfo(() -> "Location is null - returning link.");
                        finalLocation = bypassLink;
                    }
                    finalLocation = location;
                    Logger.printInfo(() -> "Resolved " + link + " -> " + location);
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

    public void openInAppBrowser(Context context, String link) {
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

    @Nullable
    public String getUserAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String access_token) {
        Logger.printInfo(() -> "Got access token!");
        accessToken = access_token;
        if (pendingUrl != null) {
            String resolveTarget = pendingUrl;
            pendingUrl = null;
            Logger.printInfo(() -> "Opening pending URL");
            performResolution(getContext(), resolveTarget);
        }
    }

    // Implementing the /s/ links class guide.
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
    //    public static boolean resolveSLink(Context context, String link) {
    //        return getInstance().resolve(context, link);
    //    }
    //
    // Your app should call this at least once. Without this, /s/ link resolution would just do
    // nothing. This function should be inserted at moment when app sets its own access_token
    // You can look for following strings in app: bearer, access_token, Authorization.
    //
    //    public static void setAppAccessToken(String access_token) {
    //        getInstance().setAccessToken(access_token);
    //    }
    //
    // This is required for both setAppAccessToken and resolveSLink.
    //
    //    public static BaseFixSLinksPatch getInstance() {
    //        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
    //        return INSTANCE;
    //    }

}