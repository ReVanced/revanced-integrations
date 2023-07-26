package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class AdsFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        boolean ads = SettingsEnum.REMOVE_ADS.getBoolean();
        if (ads && (item.isAd() || item.isWithPromotionalMusic())) list.remove();
    }
}
