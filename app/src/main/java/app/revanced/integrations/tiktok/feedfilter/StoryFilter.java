package app.revanced.integrations.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import app.revanced.integrations.tiktok.settings.Settings;

public class StoryFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_STORY.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.getIsTikTokStory();
    }
}
