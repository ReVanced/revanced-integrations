package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

import java.util.Iterator;
import java.util.List;

public class FeedItemsFilter {

    public static void filter(FeedItemList feedItemList) {
        List<IFilter> filters = List.of(new AdsFilter(), new LiveFilter(), new StoryFilter(), new ImageVideoFilter(), new ViewCountFilter(), new LikeCountFilter());
        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null) {
                for (IFilter filter : filters) {
                    filter.process(it, item);
                }
            }
        }
    }
}
