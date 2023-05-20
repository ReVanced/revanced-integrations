package app.revanced.reddit.patches;

import java.util.regex.Pattern;

public final class SanitizeUrlQueryPatch {
	private static final Pattern SANITIZE_PATTERN = Pattern.compile(".?utm_source=.+");
    public static String removeTrackingParameters(final String url) {
        return SANITIZE_PATTERN.matcher(url).replaceAll("");
    }
}
