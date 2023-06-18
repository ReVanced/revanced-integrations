package app.revanced.integrations.settingsmenu;

import android.preference.Preference;
import android.preference.PreferenceGroup;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class is used across multiple target apps.
 *
 * This entire class can _not_ reference:
 * {@link app.revanced.integrations.settings.SettingsEnum}
 * {@link app.revanced.twitch.settings.SettingsEnum}
 * {@link app.revanced.tiktok.settings.SettingsEnum}
 *
 * or any other code that references these app specific integration code.
 */
public class SettingsUtils {

    /**
     * Sort the preferences by title and ignore the casing.
     *
     * Android Preferences are automatically sorted by title,
     * but if using a localized string key it sorts on the key and not the actual title text that's used at runtime.
     *
     * @param menuDepthToSort Maximum menu depth to sort. Menus deeper than this value
     *                        will show preferences in the order created in patches.
     */
    public static void sortPreferenceGroupByTitle(PreferenceGroup group, int menuDepthToSort) {
        if (menuDepthToSort == 0) return;

        SortedMap<String, Preference> preferences = new TreeMap<>();
        for (int i = 0, prefCount = group.getPreferenceCount(); i < prefCount; i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                sortPreferenceGroupByTitle((PreferenceGroup) preference, menuDepthToSort - 1);
            }
            preferences.put(preference.getTitle().toString().toLowerCase(), preference);
        }

        int prefIndex = 0;
        for (Preference pref : preferences.values()) {
            pref.setOrder(prefIndex++);
        }
    }

}
