package org.chromium.net.impl;

import org.chromium.net.UrlRequest;

public abstract class CronetUrlRequest extends UrlRequest {

    /**
     * Method is added by patches.
     *
     * @return the Url used by the request, and is the final url if the original was redirected.
     */
    public abstract String getHookedUrl();
}
