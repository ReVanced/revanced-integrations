package app.revanced.integrations.preferences;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import app.revanced.integrations.utils.SharedPrefsHelper;

public class BooleanPreferences {
    public static boolean isTapSeekingEnabled() {
        return SharedPrefsHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_enable_tap_seeking", true);
    }

    public static boolean isExoplayerV2Enabled() {
        return SharedPrefsHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_exoplayer_v2", true);
    }

    public static boolean isCreateButtonHidden() {
        return SharedPrefsHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_create_button_hidden", true);
    }

    public static boolean isShortsButtonHidden() {
        return SharedPrefsHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_shorts_button_hidden", true);
    }
}
