package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class StoryFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        boolean story = SettingsEnum.HIDE_STORY.getBoolean();
        if (story && item.getIsTikTokStory()) list.remove();
    }
}
