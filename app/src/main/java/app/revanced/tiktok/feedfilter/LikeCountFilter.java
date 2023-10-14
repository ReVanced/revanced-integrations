package app.revanced.tiktok.feedfilter;

import app.revanced.tiktok.settings.SettingsEnum;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;

import app.revanced.tiktok.utils.Utils;

public final class LikeCountFilter implements IFilter {
    final long minLike;
    final long maxLike;

    LikeCountFilter() {
        long[] minMax = Utils.parseMinMax(SettingsEnum.MIN_MAX_LIKES);
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
