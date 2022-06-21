package app.revanced.integrations.preferences;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import app.revanced.integrations.utils.SharedPrefHelper;

public class BooleanPreferences {
    public static boolean isTapSeekingEnabled() {
        return SharedPrefHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), SharedPrefHelper.SharedPrefNames.YOUTUBE, "xfile_enable_tap_seeking", true);
    }

    public static boolean isExoplayerV2Enabled() {
        return SharedPrefHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), SharedPrefHelper.SharedPrefNames.YOUTUBE, "xfile_exoplayer_v2", true);
    }

    public static boolean isCreateButtonHidden() {
        return SharedPrefHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), SharedPrefHelper.SharedPrefNames.YOUTUBE, "xfile_create_button_hidden", true);
    }

    public static boolean isShortsButtonHidden() {
        return SharedPrefHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), SharedPrefHelper.SharedPrefNames.YOUTUBE, "xfile_shorts_button_hidden", true);
    }
}
