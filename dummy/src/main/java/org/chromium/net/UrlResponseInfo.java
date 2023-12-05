package org.chromium.net;

//dummy class
public abstract class UrlResponseInfo {

    public abstract String getUrl(); // Can return NULL for some failures.

    public abstract int getHttpStatusCode();

    // Add additional existing methods, if needed.

}
