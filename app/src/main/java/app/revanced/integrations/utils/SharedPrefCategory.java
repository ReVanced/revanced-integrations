package app.revanced.integrations.utils;

import androidx.annotation.NonNull;

public enum SharedPrefCategory {
    YOUTUBE("youtube"),
    RETURN_YOUTUBE_DISLIKE("ryd"),
    SPONSOR_BLOCK("sponsor-block"),
    REVANCED_PREFS("revanced_prefs");

    @NonNull
    public final String prefName;

    SharedPrefCategory(@NonNull String prefName) {
        this.prefName = prefName;
    }

    @NonNull
    @Override
    public String toString() {
        return prefName;
    }
}
