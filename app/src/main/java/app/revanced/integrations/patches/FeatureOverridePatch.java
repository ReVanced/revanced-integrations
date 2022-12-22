package app.revanced.integrations.patches;

public class FeatureOverridePatch {

    public static boolean overrideFeature(final String featureName) {
        return featureName.equals("subscriptions_android_is_blue_verified_field_enabled");
    }

}
