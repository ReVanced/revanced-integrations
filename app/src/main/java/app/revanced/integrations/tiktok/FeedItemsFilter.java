package app.revanced.integrations.tiktok;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

import java.util.Iterator;
import java.util.List;

public class FeedItemsFilter {
    private static boolean hideLive = false;

    public static void filter(FeedItemList feedItemList) {
        removeAds(feedItemList);
        if (hideLive) removeLive(feedItemList);
    }

    public static void enableHideLive() {
        hideLive = true;
    }

    private static void removeAds(FeedItemList feedItemList) {
        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null && item.isAd()) {
                it.remove();
            }
        }
    }

    private static void removeLive(FeedItemList feedItemList) {
        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null && item.isLive()) {
                it.remove();
            }
        }
    }
}
