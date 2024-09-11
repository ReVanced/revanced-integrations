package com.facebook.react.bridge;

import android.content.res.AssetManager;

public class CatalystInstanceImpl {
    public native void setGlobalVariable(String propName, String jsonValue);

    public void loadScriptFromAssets(AssetManager assetManager, String assetURL, boolean loadSynchronously) {
    }

    public void loadScriptFromFile(String fileName, String sourceURL, boolean loadSynchronously) {
    }

    public void loadPreloadScriptFromFile(String fileName, String sourceURL, boolean loadSynchronously) {
    }
}
