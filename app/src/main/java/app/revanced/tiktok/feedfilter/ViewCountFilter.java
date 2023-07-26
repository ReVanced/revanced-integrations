package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class ViewCountFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        long minView = Long.parseLong(SettingsEnum.MIN_MAX_VIEWS.getString().split("-")[0]);
        long maxView = Long.parseLong(SettingsEnum.MIN_MAX_VIEWS.getString().split("-")[1]);
        AwemeStatistics statistics = item.getStatistics();
        if (statistics != null) {
            long playCount = statistics.getPlayCount();
            if (playCount < minView || playCount > maxView) list.remove();
        }
    }
}
