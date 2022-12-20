package app.revanced.integrations.patches;

import static app.revanced.integrations.patches.LithoThemePatch.isShortsCommentBox;

import app.revanced.integrations.settings.SettingsEnum;

public final class ShortsCommentBoxColorPatch extends Filter {
    public ShortsCommentBoxColorPatch() {
        this.pathRegister.registerAll(
            new BlockRule(SettingsEnum.SHORTS_COMMENT_BOX_COLOR, "comment_composer")
        );
    }

    public boolean filter(final String path, final String identifier) {
        isShortsCommentBox = pathRegister.contains(path);

        return false;
    }
}
