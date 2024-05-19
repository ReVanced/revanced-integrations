package app.revanced.integrations.shared.fixes.slink;


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
    String accessToken = null;
    public String pendingUrl = null;

    public ResolveResult performResolution(Context context, String link) {
        if (link.matches(".*reddit\\.com/r/[^/]+/s/[^/]+")) {
            Logger.printInfo(() -> "Resolving " + link);
            if (link.endsWith("#bypass")) {
                openInAppBrowser(context, link);
                return ResolveResult.DO_NOTHING;
            }
            String accessToken = getUserAccessToken(context);
            if (accessToken == null) {
                // this is terrible!
                // but we need to get access_token to properly auth request, especially if user is
                // using VPN or similar
                Intent startIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                context.startActivity(startIntent);
                return ResolveResult.ACCESS_TOKEN_START;
            }
            String bypassLink = link + "#bypass";
            Utils.runOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    String finalLocation = bypassLink;
                    try {
                        // Disable strict mode in order to allow network access on the main thread.
                        // This is not ideal, but it's the easiest solution for now.
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
                }
            });
            return ResolveResult.DO_NOTHING;
        }

        return ResolveResult.CONTINUE;
    }

    protected abstract void openInAppBrowser(Context context, String link);

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
    public String getUserAccessToken(Context context) {
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
    }}