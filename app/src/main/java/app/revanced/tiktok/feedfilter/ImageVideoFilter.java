package app.revanced.tiktok.feedfilter;

import com.ss.android.ugc.aweme.feed.model.Aweme;

import java.util.Iterator;

import app.revanced.tiktok.settings.SettingsEnum;

public class ImageVideoFilter implements IFilter {
    @Override
    public void process(Iterator<Aweme> list, Aweme item) {
        boolean image = SettingsEnum.HIDE_IMAGE.getBoolean();
        if (image && (item.isImage() || item.isPhotoMode())) list.remove();
    }
}
