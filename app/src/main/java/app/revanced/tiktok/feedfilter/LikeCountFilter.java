package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class LikeCountFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        long minLike = Long.parseLong(SettingsEnum.HIDE_LIKE_COUNT.getString().split("-")[0]);
        long maxLike = Long.parseLong(SettingsEnum.HIDE_LIKE_COUNT.getString().split("-")[1]);
        AwemeStatistics statistics = item.getStatistics();
        if (statistics != null) {
            long likeCount = statistics.getDiggCount();
            if (likeCount < minLike || likeCount > maxLike) list.remove();
        }
    }
}
