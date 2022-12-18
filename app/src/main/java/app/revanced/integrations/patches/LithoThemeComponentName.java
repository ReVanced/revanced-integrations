package app.revanced.integrations.patches;

import static app.revanced.integrations.patches.LithoThemePatch.shortsCommentBox;

public final class LithoThemeComponentName extends Filter {
    public boolean filter(final String path, final String identifier) {
        shortsCommentBox = path.contains("comment_composer");

        return false;
    }
}
