package app.revanced.integrations.patches.components;

import android.os.Build;

import androidx.annotation.RequiresApi;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class PlayerFlyoutMenuItemsFilter extends Filter {
    private static StringFilterGroup playerFlyoutPanelRule;
    private static String[] exceptions;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PlayerFlyoutMenuItemsFilter() {
        playerFlyoutPanelRule = new StringFilterGroup(
                null,
                "overflow_menu_item"
        );

        exceptions = new String[]{
                "comment",
                "_sheet_"
        };

        protobufBufferFilterGroups.addAll(
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_CAPTIONS_MENU,
                        "yt_outline_closed_caption"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_LOOP_VIDEO_MENU,
                        "yt_outline_arrow_repeat_1_"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_AMBIENT_MODE_MENU,
                        "yt_outline_screen_light"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_REPORT_MENU,
                        "yt_outline_flag"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_HELP_MENU,
                        "yt_outline_question_circle"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_MORE_INFO_MENU,
                        "yt_outline_info_circle"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_SPEED_MENU,
                        "yt_outline_play_arrow_half_circle"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_AUDIO_TRACK_MENU,
                        "yt_outline_person_radar"
                ),
                new ByteArrayAsStringFilterGroup(
                        SettingsEnum.HIDE_WATCH_IN_VR_MENU,
                        "yt_outline_vr"
                )
        );
    }

    private boolean isEveryFilterGroupEnabled() {
        for (ByteArrayFilterGroup rule : protobufBufferFilterGroups)
            if (!rule.isEnabled()) return false;

        return true;
    }

    @Override
    boolean isFiltered(String path, String identifier, byte[] _protobufBufferArray) {
        if (ReVancedUtils.containsAny(path, exceptions)) return false;

        if (isEveryFilterGroupEnabled())
            if (playerFlyoutPanelRule.check(identifier).isFiltered()) return true;

        return super.isFiltered(path, identifier, _protobufBufferArray);
    }
}
