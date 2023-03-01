package app.revanced.integrations.sponsorblock.player.ui;

import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.SharedPrefCategory;

public class ButtonVisibility {
    public static Visibility getButtonVisibility(String key) {
        return getButtonVisibility(key, SharedPrefCategory.YOUTUBE);
    }

    public static Visibility getButtonVisibility(String key, SharedPrefCategory name) {
        String value = SharedPrefHelper.getString(name, key, null);

        if (value == null || value.isEmpty()) return Visibility.NONE;

        switch (value.toUpperCase()) {
            case "PLAYER":
                return Visibility.PLAYER;
            case "BUTTON_CONTAINER":
                return Visibility.BUTTON_CONTAINER;
            case "BOTH":
                return Visibility.BOTH;
            default:
                return Visibility.NONE;
        }
    }

    public static boolean isVisibleInContainer(String key) {
        return isVisibleInContainer(getButtonVisibility(key));
    }

    public static boolean isVisibleInContainer(String key, SharedPrefCategory name) {
        return isVisibleInContainer(getButtonVisibility(key, name));
    }

    public static boolean isVisibleInContainer(Visibility visibility) {
        return visibility == Visibility.BOTH || visibility == Visibility.BUTTON_CONTAINER;
    }
}
