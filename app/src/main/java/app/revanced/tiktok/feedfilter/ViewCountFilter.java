package app.revanced.tiktok.feedfilter;

import static app.revanced.tiktok.utils.TikTokUtils.parseMinMax;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import app.revanced.tiktok.settings.SettingsEnum;

public class ViewCountFilter implements IFilter {
    final long minView;
    final long maxView;

    ViewCountFilter() {
        long[] minMax = parseMinMax(SettingsEnum.MIN_MAX_VIEWS);
        minView = minMax[0];
        maxView = minMax[1];
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        AwemeStatistics statistics = item.getStatistics();
        if (statistics == null) return false;

        long playCount = statistics.getPlayCount();
        return playCount < minView || playCount > maxView;
    }
}
