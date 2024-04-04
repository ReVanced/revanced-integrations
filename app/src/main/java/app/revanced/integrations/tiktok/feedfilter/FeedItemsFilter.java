package app.revanced.integrations.tiktok.feedfilter;

import java.util.Iterator;
import java.util.List;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

public final class FeedItemsFilter {
    private static final List<IFilter> FILTERS = List.of(
            new AdsFilter(),
            new LiveFilter(),
            new StoryFilter(),
            new ImageVideoFilter(),
            new ViewCountFilter(),
            new LikeCountFilter()
    );

    public static void filter(FeedItemList feedItemList) {
        Iterator<Aweme> feedItemListIterator = feedItemList.items.iterator();
        while (feedItemListIterator.hasNext()) {
            Aweme item = feedItemListIterator.next();
            if (item == null) continue;

            for (IFilter filter : FILTERS) {
                boolean enabled = filter.getEnabled();
                if (enabled && filter.getFiltered(item)) {
                    feedItemListIterator.remove();
                    break;
                }
            }
        }
    }
}
