package app.revanced.integrations.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import app.revanced.integrations.tiktok.settings.Settings;

public class LiveFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_LIVE.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.isLive() || item.isLiveReplay();
    }
}
