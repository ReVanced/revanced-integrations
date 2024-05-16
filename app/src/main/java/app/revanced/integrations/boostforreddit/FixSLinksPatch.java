package app.revanced.integrations.boostforreddit;

import android.content.Context;
import android.content.Intent;

import com.rubenmayayo.reddit.ui.activities.WebViewActivity;

import app.revanced.integrations.shared.fixes.slink.JRAWFixSLinksPatch;
import app.revanced.integrations.shared.fixes.slink.ResolveResult;

public class FixSLinksPatch extends JRAWFixSLinksPatch {
    private static JRAWFixSLinksPatch INSTANCE;
    private FixSLinksPatch() {
    }

    public static JRAWFixSLinksPatch getInstance() {
        if (INSTANCE == null) INSTANCE = new app.revanced.integrations.boostforreddit.FixSLinksPatch();
        return INSTANCE;
    }

    public static boolean resolveSLink(Context context, String link) {
        JRAWFixSLinksPatch instance = getInstance();
        ResolveResult res = instance.performResolution(context, link);
        boolean ret = false;
        switch (res) {
            case ACCESS_TOKEN_START: {
                instance.pendingUrl = link;
                ret = true;
                break;
            }
            case DO_NOTHING:
                ret = true;
                break;
            default:
                break;
        }
        return ret;
    }
    public static void JrawHookGetAccessToken(String access_token) {
        getInstance().setAccessToken(access_token);
    }

    @Override
    public ResolveResult performResolution(Context context, String link) {
        return super.performResolution(context, link);
    }

    @Override
    public void openInAppBrowser(Context context, String link) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("url", link);
        context.startActivity(intent);
    }
}
