package app.revanced.integrations.shared.settings.preference;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.requests.Route.Method.GET;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.ThemeHelper;
import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.requests.Route;

/**
 * Opens a dialog showing the links from {@link #SOCIAL_LINKS_PROVIDER}.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ReVancedAboutPreference extends Preference {

    private static class ReVancedSocialLink {
        final boolean preferred;
        final String name;
        final String url;
        final String favIconUrl;

        ReVancedSocialLink(JSONObject json) throws JSONException {
            this(json.getBoolean("preferred"),
                    json.getString("name"),
                    json.getString("url")
            );
        }

        ReVancedSocialLink(boolean preferred, String name, String url) {
            this.name = name;
            this.url = url;
            this.preferred = preferred;
            // Parse the domain name and append /favicon.ico
            final int httpEndIndex = url.indexOf("//");
            final int domainStartIndex = httpEndIndex > 0 ? httpEndIndex + 2 : 0;
            final int pathStartIndex = url.indexOf("/", domainStartIndex);
            final int domainEndIndex = pathStartIndex > 0 ? pathStartIndex : url.length();
            favIconUrl = url.substring(0, domainEndIndex) + "/favicon.ico";
        }

        @NonNull
        @Override
        public String toString() {
            return "ReVancedSocialLink{" +
                    "preferred=" + preferred +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", favIconUrl='" + favIconUrl + '\'' +
                    '}';
        }
    }

    /**
     * Links to use if fetch links api call fails.
     */
    private static final ReVancedSocialLink[] NO_CONNECTION_STATIC_LINKS = {
            new ReVancedSocialLink(true, "ReVanced.app", "https://ReVanced.app")
    };

    @Nullable
    private static volatile ReVancedSocialLink[] fetchedLinks;

    private static final String SOCIAL_LINKS_PROVIDER = "https://api.revanced.app/v2/socials";
    private static final Route.CompiledRoute GET_SOCIAL = new Route(GET, "?source=appSettings").compile();

    private static ReVancedSocialLink[] fetchSocialLinks() {
        try {
            if (fetchedLinks != null) return fetchedLinks;

            // Check if there is internet connection
            if (!Utils.isNetworkConnected()) return NO_CONNECTION_STATIC_LINKS;

            HttpURLConnection connection = Requester.getConnectionFromCompiledRoute(SOCIAL_LINKS_PROVIDER, GET_SOCIAL);
            Logger.printDebug(() -> "Fetching social links from: " + connection.getURL());

            // Do not show the announcement if the request failed.
            final int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Logger.printDebug(() -> "Failed to get social links.  Response code: " + responseCode);
                return NO_CONNECTION_STATIC_LINKS;
            }

            String jsonString = Requester.parseInputStreamAndClose(connection.getInputStream(), false);
            JSONArray socials = new JSONObject(jsonString).getJSONArray("socials");

            List<ReVancedSocialLink> links = new ArrayList<>();

            for (int i = 0, length = socials.length(); i < length; i++) {
                ReVancedSocialLink link = new ReVancedSocialLink(socials.getJSONObject(i));
                links.add(link);
            }

            Logger.printDebug(() -> "links: " + links);

            return fetchedLinks = links.toArray(new ReVancedSocialLink[0]);

        } catch (JSONException ex) {
            Logger.printException(() -> "Could not parse about information", ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to get about information", ex);
        }

        return NO_CONNECTION_STATIC_LINKS;
    }

    private static String useNonBreakingHyphens(String text) {
        // Replace any dashes with non breaking dashes, so the English text 'pre-release'
        // and the dev release number does not break and cover two lines.
        return text.replace("-", "&#8209;"); // #8209 = non breaking hyphen.
    }

    private static String getResourceColorHexString(boolean isDarkTheme) {
        final int color = Utils.getResourceColor(isDarkTheme ? "yt_black1" : "yt_white1");
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private static String createDialogHtml(ReVancedSocialLink[] socialLinks) {
        final boolean isNetworkConnected = Utils.isNetworkConnected();

        // Use a dark theme if needed.
        final boolean isDarkTheme = ThemeHelper.isDarkTheme();
        String background = getResourceColorHexString(isDarkTheme);
        String foreground = getResourceColorHexString(!isDarkTheme);
        String lightDarkStyle = "background-color: " + background + "; color: " + foreground + ";";

        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<body style=\"text-align: center; padding: 10px; ")
                .append(lightDarkStyle).append("\">");

        if (isNetworkConnected) {
            builder.append("<img style=\"width: 100px; height: 100px;\" "
                    // Hide any images that don't load.
                    + "onerror=\"this.style.display='none';\" "
                    + "src=\"https://revanced.app/favicon.ico\" />");
        }

        // Add a disclaimer if using a dev release.
        String patchesVersion = Utils.getPatchesReleaseVersion();
        if (patchesVersion.contains("-dev")) {
            // Replace hyphens with no breaking dashes,
            // so the version number and the English word "pre-release" do not break lines.
            builder.append("<h4>")
                    .append(useNonBreakingHyphens(str("revanced_settings_about_links_dev_header")))
                    .append("</h4>");
            builder.append("<p>")
                    .append(useNonBreakingHyphens(str("revanced_settings_about_links_dev_body1", patchesVersion)))
                    .append("</p>");
            builder.append("<p>")
                    .append(str("revanced_settings_about_links_dev_body2"))
                    .append("</p>");
        }

        builder.append("<h3>")
                .append(str("revanced_settings_about_links_header"))
                .append("</h3>");

        builder.append("<div style=\"display: inline-block;\">");
        for (ReVancedSocialLink social : socialLinks) {
            builder.append("<div style=\"margin-bottom: 20px; text-align: left;\">");
            if (isNetworkConnected) {
                builder.append(String.format("<img src=\"%s\" style=\"vertical-align: middle; width: 24px; height: 24px;\" "
                        + "onerror=\"this.style.display='none';\" />", social.favIconUrl));
            }
            builder.append(String.format("<a href=\"%s\" style=\"margin-left: 5px;\">%s</a>", social.url, social.name));
            builder.append("</div>");
        }
        builder.append("</div>");

        builder.append("</body></html>");
        return builder.toString();
    }

    {
        setOnPreferenceClickListener(pref -> {
            Context context = getContext();

            // Show a progress spinner if the social links are not fetched yet.
            final ProgressDialog progress;
            if (fetchedLinks == null && Utils.isNetworkConnected()) {
                progress = new ProgressDialog(context);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            } else {
                progress = null;
            }

            Utils.runOnBackgroundThread(() -> {
                ReVancedSocialLink[] socialLinks = fetchSocialLinks();
                String htmlDialog = createDialogHtml(socialLinks);

                Utils.runOnMainThread(() -> {
                    if (progress != null) {
                        progress.dismiss();
                    }

                    new WebViewDialog(context, htmlDialog).show();
                });
            });

            return false;
        });
    }

    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReVancedAboutPreference(Context context) {
        super(context);
    }
}

/**
 * Displays html content as a dialog. Any links a user taps on are opened in an external browser.
 */
class WebViewDialog extends Dialog {

    private final String htmlContent;

    public WebViewDialog(@NonNull Context context, @NonNull String htmlContent) {
        super(context);
        this.htmlContent = htmlContent;
    }

    // JS required to hide any broken images.  No remote javascript is ever loaded.
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WebView webView = new WebView(getContext());
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        // JS used to hide any broken images.  No remote JS is ever loaded.
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new OpenLinksExternallyWebClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);

        setContentView(webView);
    }

    private class OpenLinksExternallyWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getContext().startActivity(intent);
            } catch (Exception ex) {
                Logger.printException(() -> "Open link failure", ex);
            }
            dismiss();
            return true;
        }
    }
}
