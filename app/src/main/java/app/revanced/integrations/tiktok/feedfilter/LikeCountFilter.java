package app.revanced.integrations.tiktok.feedfilter;

import static app.revanced.integrations.tiktok.Utils.parseMinMax;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import app.revanced.integrations.tiktok.settings.Settings;

public final class LikeCountFilter implements IFilter {
    final long minLike;
    final long maxLike;

    LikeCountFilter() {
        long[] minMax = parseMinMax(Settings.MIN_MAX_LIKES);
        minLike = minMax[0];
        maxLike = minMax[1];
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public boolean getFiltered(Aweme item) {
        AwemeStatistics statistics = item.getStatistics();
        if (statistics == null) return false;

        long likeCount = statistics.getDiggCount();
        return likeCount < minLike || likeCount > maxLike;
    }
}
