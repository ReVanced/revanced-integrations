package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Setting;

public class VideoAdsPatch {

    // Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    // depends on Whitelist patch (still needs to be written)
    public static boolean shouldShowAds() {
        return !Setting.HIDE_VIDEO_ADS.getBoolean(); // TODO && Whitelist.shouldShowAds();
    }

}
