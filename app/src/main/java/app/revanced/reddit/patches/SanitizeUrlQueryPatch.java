package app.revanced.reddit.patches;

public class UrlSanitizer {
    public static String removeTrackingParameters(String url) {
        Pattern pattern = Pattern.compile(".?utm_source=.+")
        Matcher matcher = pattern.matcher(url)
        return matcher.replaceAll("")
    }
}
