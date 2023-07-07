package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

// Abuse LithoFilter for CustomVideoSpeedPatch.
public final class VideoSpeedMenuFilterPatch extends Filter {
    public static boolean isVideoSpeedMenuVisible;

    public VideoSpeedMenuFilterPatch() {
        pathFilterGroups.addAll(new StringFilterGroup(
                SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_ENABLED,
                "playback_speed_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        isVideoSpeedMenuVisible = super.isFiltered(path, identifier, protobufBufferArray);

        return false;
    }
}
