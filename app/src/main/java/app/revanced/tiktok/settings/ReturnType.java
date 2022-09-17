package app.revanced.tiktok.settings;

import merger.MergeIf;

@MergeIf(packageName = {"com.ss.android.ugc.trill", "com.zhiliaoapp.musically"})
public enum ReturnType {
    BOOLEAN, INTEGER, STRING, LONG, FLOAT
}
