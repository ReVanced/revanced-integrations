package app.revanced.integrations.youtube.patches.components;

import app.revanced.integrations.youtube.settings.SettingsEnum;

@SuppressWarnings("unused")
final class CommentsFilter extends Filter {

    public CommentsFilter() {
        var comments = new StringFilterGroup(
                SettingsEnum.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var previewComment = new StringFilterGroup(
                SettingsEnum.HIDE_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        addPathCallbacks(
                comments,
                previewComment
        );
    }
}
