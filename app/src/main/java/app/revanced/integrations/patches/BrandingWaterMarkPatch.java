package app.revanced.integrations.patches;

import app.revanced.integrations.adremover.AdRemoverAPI;

public class BrandingWaterMarkPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1wpcVsYZh9iegifZthlX0-JY5ExYhq8oc/view?usp=sharing for where it needs to be used.
    public static int BrandingWatermark(int defaultValue) {
        return AdRemoverAPI.BrandingWatermark(defaultValue);
    }
}
