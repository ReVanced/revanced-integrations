package app.revanced.integrations.syncforreddit;

import app.revanced.integrations.syncforreddit.internal.RedditVideoPlaylistParser;

/**
 * @noinspection unused
 */
public class FixRedditVideoDownloadPatch {

    public static String[] getLinks(byte[] data) {
        return RedditVideoPlaylistParser.INSTANCE.parse(data);
    }
}
