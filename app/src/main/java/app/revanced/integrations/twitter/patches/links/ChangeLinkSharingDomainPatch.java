package app.revanced.integrations.twitter.patches.links;

public final class ChangeLinkSharingDomainPatch {
    static final String DOMAIN_NAME = "https://fxtwitter.com";
    static final String LINK_FORMAT = "%s/%s/status/%s";

    public static String formatResourceLink(Object... formatArgs) {
        String username = (String) formatArgs[0];
        String tweetId = (String) formatArgs[1];
        return formatSharingLink(tweetId, username);
    }

    public static String formatLink(long tweetId, String username) {
        String tweetIdStr = Long.toString(tweetId);
        return formatSharingLink(tweetIdStr, username);
    }

    private static String formatSharingLink(String tweetId, String username) {
        return String.format(LINK_FORMAT, DOMAIN_NAME, username, tweetId);
    }
}
