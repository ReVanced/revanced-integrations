package app.revanced.shared.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceGroup;

import androidx.annotation.NonNull;

import java.util.SortedMap;
import java.util.TreeMap;

import kotlin.text.Regex;

/**
 * Class is used across multiple target apps.
 *
 * This entire class can _not_ reference:
 * {@link app.revanced.integrations.settings.SettingsEnum}
 * {@link app.revanced.twitch.settings.SettingsEnum}
 * {@link app.revanced.tiktok.settings.SettingsEnum}
 *
 * or any other code that references these app specific integration classes.
 */
public class SettingsUtils {

    private static final Regex punctuationRegex = new Regex("\\p{P}+");

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
            preferences.put(removePunctuationConvertToLowercase(preference.getTitle()), preference);
        }

        int prefIndex = 0;
        for (Preference pref : preferences.values()) {
            int indexToSet = prefIndex++;
            if (pref instanceof PreferenceGroup) {
                // Place preference groups last.
                // Use an offset to push the group to the end.
                indexToSet += 1000;
            }
            pref.setOrder(indexToSet);
        }
    }

    private static String removePunctuationConvertToLowercase(CharSequence original) {
        return punctuationRegex.replace(original, "").toLowerCase();
    }

    public static void restartApp(@NonNull Context context) {
        String packageName = context.getPackageName();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
        // Required for API 34 and later
        // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
        mainIntent.setPackage(packageName);
        context.startActivity(mainIntent);
        System.exit(0);
    }
}
