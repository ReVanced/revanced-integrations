package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class LiveFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        boolean live = SettingsEnum.HIDE_LIVE.getBoolean();
        if (live && (item.isLive() || item.isLiveReplay())) list.remove();
    }
}
