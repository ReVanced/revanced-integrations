package app.revanced.integrations.preferences;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import fi.razerman.youtube.Helpers.SharedPrefs;

public class BooleanPreferences {
    public static boolean isTapSeekingEnabled() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_enable_tap_seeking", true);
    }

    public static boolean isExoplayerV2Enabled() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_exoplayer_v2", true);
    }

    public static boolean isCreateButtonHidden() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_create_button_hidden", true);
    }

    public static boolean isShortsButtonHidden() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_shorts_button_hidden", true);
    }
}
