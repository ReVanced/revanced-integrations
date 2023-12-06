package org.chromium.net.impl;

import org.chromium.net.UrlRequest;

public abstract class CronetUrlRequest extends UrlRequest {

    /**
     * Method is added by patches.
     */
    public abstract String getHookedUrl();
}
