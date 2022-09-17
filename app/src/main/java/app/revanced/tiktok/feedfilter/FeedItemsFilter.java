package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

import java.util.Iterator;
import java.util.List;

import app.revanced.tiktok.settings.SettingsEnum;
import merger.MergeIf;

@MergeIf(packageName = {"com.ss.android.ugc.trill", "com.zhiliaoapp.musically"})
public class FeedItemsFilter {

    public static void filter(FeedItemList feedItemList) {
        if (SettingsEnum.TIK_REMOVE_ADS.getBoolean()) removeAds(feedItemList);
        if (SettingsEnum.TIK_HIDE_LIVE.getBoolean()) removeLive(feedItemList);
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
