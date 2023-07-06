package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

// Abuse LithoFilter for OldVideoQualityMenuPatch.
public final class VideoQualityMenuFilterPatch extends Filter {
    public static boolean isVideoQualityMenuVisible;


    public VideoQualityMenuFilterPatch() {
        pathFilterGroups.addAll(new StringFilterGroup(
                SettingsEnum.SHOW_OLD_VIDEO_QUALITY_MENU,
                "quick_quality_sheet_content.eml-js"
        ));
    }

    @Override
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray) {
        isVideoQualityMenuVisible = super.isFiltered(path, identifier, protobufBufferArray);

        // Always return false. This class is only used to set isVideoQualityMenuVisible.
        return false;
    }
}
