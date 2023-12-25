package app.revanced.integrations.youtube.patches;

import android.view.View;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.utils.ReVancedUtils;

public final class HideFilterBarPatch {
    public static int hideInFeed(final int height) {
        if (Settings.HIDE_FILTER_BAR_FEED_IN_FEED.getBoolean()) return 0;

        return height;
    }

    public static void hideInRelatedVideos(final View chipView) {
        if (!Settings.HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS.getBoolean()) return;

        ReVancedUtils.hideViewByLayoutParams(chipView);
    }

    public static int hideInSearch(final int height) {
        if (Settings.HIDE_FILTER_BAR_FEED_IN_SEARCH.getBoolean()) return 0;

        return height;
    }
}
