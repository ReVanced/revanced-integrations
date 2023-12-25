package app.revanced.integrations.youtube.patches.components;

import app.revanced.integrations.youtube.settings.Setting;

@SuppressWarnings("unused")
final class CommentsFilter extends Filter {

    public CommentsFilter() {
        var comments = new StringFilterGroup(
                Setting.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var previewComment = new StringFilterGroup(
                Setting.HIDE_PREVIEW_COMMENT,
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
