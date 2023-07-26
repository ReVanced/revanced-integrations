package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import java.util.Iterator;

public interface IFilter {
    void process(Iterator<Aweme> list, Aweme item);
}
