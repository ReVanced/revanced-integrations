package app.revanced.integrations.boostforreddit;

import com.rubenmayayo.reddit.ui.activities.WebViewActivity;

import app.revanced.integrations.shared.fixes.slink.BaseFixSLinksPatch;

/** @noinspection unused*/
public class FixSLinksPatch extends BaseFixSLinksPatch {
    private FixSLinksPatch() {
        webViewActivityClass = WebViewActivity.class;
    }

    public static boolean patchResolveSLink(String link) {
        return getInstance().resolveSLink(link);
    }

    public static void patchSetAccessToken(String accessToken) {
        getInstance().setAccessToken(accessToken);
    }
    public static BaseFixSLinksPatch getInstance() {
        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
        return INSTANCE;
    }
}
