package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeStatistics;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;

import java.util.Iterator;
import java.util.List;

import app.revanced.tiktok.settings.SettingsEnum;

public class FeedItemsFilter {

    public static void filter(FeedItemList feedItemList) {
        boolean ads = SettingsEnum.TIK_REMOVE_ADS.getBoolean();
        boolean live = SettingsEnum.TIK_HIDE_LIVE.getBoolean();
        boolean story = SettingsEnum.TIK_HIDE_STORY.getBoolean();
        boolean image = SettingsEnum.TIK_HIDE_IMAGE.getBoolean();
        long minView = SettingsEnum.TIK_HIDE_PLAY_COUNT_MIN.getLong();
        long maxView = SettingsEnum.TIK_HIDE_PLAY_COUNT_MAX.getLong();
        long minLike = SettingsEnum.TIK_HIDE_LIKE_COUNT_MIN.getLong();
        long maxLike = SettingsEnum.TIK_HIDE_LIKE_COUNT_MAX.getLong();

        List<Aweme> items = feedItemList.items;
        Iterator<Aweme> it = items.iterator();
        while (it.hasNext()) {
            Aweme item = it.next();
            if (item != null) {
                if (ads && (item.isAd() || item.isWithPromotionalMusic())) it.remove();
                if (live && (item.isLive() || item.isLiveReplay())) it.remove();
                if (story && item.getIsTikTokStory()) it.remove();
                if (image && (item.isImage() || item.isPhotoMode())) it.remove();
                AwemeStatistics statistics = item.getStatistics();
                if (statistics != null) {
                    long playCount = statistics.getPlayCount();
                    long likeCount = statistics.getDiggCount();
                    if (playCount < minView || playCount > maxView) it.remove();
                    if (likeCount < minLike || likeCount > maxLike) it.remove();
                }
            }
        }
    }
}
