package app.revanced.integrations.shared.fixes.slink;

import static app.revanced.integrations.shared.Utils.getContext;

import android.content.Context;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;

public abstract class JRAWFixSLinksPatch implements IFixSLinksPatch {
    String accessToken = null;
    public String pendingUrl = null;

    @Override
    public void setAccessToken(String access_token) {
        Logger.printInfo(() -> "Got access token!");
        accessToken = access_token;
        if (pendingUrl != null) {
            String resolveTarget = pendingUrl;
            pendingUrl = null;
            Logger.printInfo(() -> "Opening pending URL");
            performResolution(getContext(), resolveTarget);
        }
    }

    @Override
    @Nullable
    public String getUserAccessToken(Context context) {
        return accessToken;
    }
}
