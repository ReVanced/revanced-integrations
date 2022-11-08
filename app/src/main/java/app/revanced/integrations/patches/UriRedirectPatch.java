package app.revanced.integrations.patches;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.integrations.settings.SettingsEnum;

public class UriRedirectPatch {

    public static String parseRedirectUri(String uri) {
        if (SettingsEnum.ENABLE_URI_REDIRECT.getBoolean()) {
            Matcher matcher = Pattern.compile("&q=(http.+?)&v=").matcher(uri);
            return matcher.find() ? URLDecoder.decode(matcher.group(1)) : uri;
        }
        return uri;
    }
}
