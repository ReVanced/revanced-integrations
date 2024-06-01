package app.revanced.integrations.boostforreddit;

import android.content.Context;

import com.rubenmayayo.reddit.ui.activities.WebViewActivity;

import app.revanced.integrations.shared.fixes.slink.BaseFixSLinksPatch;

/** @noinspection unused*/
public class FixSLinksPatch extends BaseFixSLinksPatch {
    private FixSLinksPatch() {
        webViewActivity = WebViewActivity.class;
    }

    public static boolean patchResolveSLink(Context context, String link) {
        return getInstance().resolveSLink(context, link);
    }

    public static void patchSetAccessToken(String access_token) {
        getInstance().setAccessToken(access_token);
    }

    public static BaseFixSLinksPatch getInstance() {
        if (INSTANCE == null) INSTANCE = new FixSLinksPatch();
        return INSTANCE;
    }
}
