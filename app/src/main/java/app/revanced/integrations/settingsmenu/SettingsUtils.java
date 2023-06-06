package app.revanced.integrations.settingsmenu;

import android.preference.Preference;
import android.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     */
    public static void sortPreferenceScreenByTitle(PreferenceScreen screen) {
        final int prefCount = screen.getPreferenceCount();
        List<Preference> preferences = new ArrayList<>(prefCount);
        for (int i = 0; i < prefCount; i++) {
            Preference preference = screen.getPreference(i);
            if (preference instanceof PreferenceScreen) {
                sortPreferenceScreenByTitle((PreferenceScreen)preference);
            }
            preferences.add(preference);
        }
        Collections.sort(preferences, (pref1, pref2) ->
                pref1.getTitle().toString().toLowerCase().compareTo(pref2.getTitle().toString().toLowerCase())
        );

        int prefIndex = 0;
        for (Preference pref : preferences) {
            pref.setOrder(prefIndex++);
        }
    }

}
