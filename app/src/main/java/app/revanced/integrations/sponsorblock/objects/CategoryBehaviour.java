package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.StringRef.sf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.sponsorblock.StringRef;

public enum CategoryBehaviour {
    SKIP_AUTOMATICALLY("skip", 2, sf("sb_skip_automatically"), true),
    // desktop does not have skip-once behavior. Key is unique to ReVanced
    SKIP_AUTOMATICALLY_ONCE("skip-once", 3, sf("sb_skip_automatically_once"), true),
    MANUAL_SKIP("manual-skip", 1, sf("sb_skip_showbutton"), false),
    SHOW_IN_SEEKBAR("seekbar-only", 0, sf("sb_skip_seekbaronly"), false),
    IGNORE("ignore", -1, sf("sb_skip_ignore"), false);

    @NonNull
    public final String key;
    public final int desktopKey;
    @NonNull
    public final StringRef name;
    /**
     * If the segment should skip automatically
     */
    public final boolean skip;

    CategoryBehaviour(String key,
                      int desktopKey,
                      StringRef name,
                      boolean skip) {
        this.key = Objects.requireNonNull(key);
        this.desktopKey = desktopKey;
        this.name = Objects.requireNonNull(name);
        this.skip = skip;
    }

    @Nullable
    public static CategoryBehaviour byStringKey(@NonNull String key) {
        for (CategoryBehaviour behaviour : CategoryBehaviour.values()){
            if (behaviour.key.equals(key)) {
                return behaviour;
            }
        }
        return null;
    }

    @Nullable
    public static CategoryBehaviour byDesktopKey(int desktopKey) {
        for (CategoryBehaviour behaviour : values()) {
            if (behaviour.desktopKey == desktopKey) {
                return behaviour;
            }
        }
        return null;
    }
}
