package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.objects.CategoryBehaviour;
import app.revanced.integrations.sponsorblock.objects.SegmentCategory;
import app.revanced.integrations.sponsorblock.ui.SponsorBlockViewController;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class SponsorBlockSettings {
    public static final String SEGMENT_CATEGORY_COLOR_PREFERENCE_KEY_SUFFIX = "_color";
    public static String sponsorBlockAPIFetchCategories = "[]";

    public static void importSettings(@NonNull String json) {
        ReVancedUtils.verifyOnMainThread();
        try {
            JSONObject settingsJson = new JSONObject(json);

            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");

            SharedPreferences.Editor editor = SharedPrefHelper.getPreferences(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK).edit();

            SegmentCategory[] categories = SegmentCategory.valuesWithoutUnsubmitted();
            for (SegmentCategory category : categories) {
                String categoryKey = category.key;
                JSONObject categoryObject = barTypesObject.getJSONObject(categoryKey);
                String color = categoryObject.getString("color");

                editor.putString(categoryKey + SEGMENT_CATEGORY_COLOR_PREFERENCE_KEY_SUFFIX, color);
                editor.putString(categoryKey, CategoryBehaviour.IGNORE.key);
            }

            for (int i = 0; i < categorySelectionsArray.length(); i++) {
                JSONObject categorySelectionObject = categorySelectionsArray.getJSONObject(i);

                String categoryKey = categorySelectionObject.getString("name");
                SegmentCategory category = SegmentCategory.byCategoryKey(categoryKey);
                if (category == null) {
                    continue; // unsupported category, ignore
                }

                final int desktopKey = categorySelectionObject.getInt("option");
                CategoryBehaviour behaviour = CategoryBehaviour.byDesktopKey(desktopKey);
                if (behaviour != null) {
                    editor.putString(category.key, behaviour.key);
                } else {
                    LogHelper.printException(() -> "Unknown segment category behavior key: " + desktopKey);
                }
            }
            editor.apply();

            SettingsEnum.SB_UUID.saveValue(settingsJson.getString("userID"));
            SettingsEnum.SB_IS_VIP.saveValue(settingsJson.getBoolean("isVip"));
            SettingsEnum.SB_API_URL.saveValue(settingsJson.getString("serverAddress"));
            SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.saveValue(!settingsJson.getBoolean("dontShowNotice"));
            SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.saveValue(settingsJson.getBoolean("showTimeWithSkips"));
            SettingsEnum.SB_MIN_DURATION.saveValue(Float.valueOf(settingsJson.getString("minDuration")));
            SettingsEnum.SB_COUNT_SKIPS.saveValue(settingsJson.getBoolean("trackViewCount"));

            ReVancedUtils.showToastLong(str("sb_settings_import_successful"));
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to import settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("sb_settings_import_failed"));
        }
    }

    @NonNull
    public static String exportSettings() {
        ReVancedUtils.verifyOnMainThread();
        try {
            LogHelper.printDebug(() -> "Creating SponsorBlock export settings string");
            JSONObject json = new JSONObject();

            JSONObject barTypesObject = new JSONObject(); // categories' colors
            JSONArray categorySelectionsArray = new JSONArray(); // categories' behavior

            SegmentCategory[] categories = SegmentCategory.valuesWithoutUnsubmitted();
            for (SegmentCategory category : categories) {
                JSONObject categoryObject = new JSONObject();
                String categoryKey = category.key;
                categoryObject.put("color", SponsorBlockUtils.formatColorString(category.color));
                barTypesObject.put(categoryKey, categoryObject);

                int desktopKey = category.behaviour.desktopKey;
                if (desktopKey != -1) {
                    JSONObject behaviorObject = new JSONObject();
                    behaviorObject.put("name", categoryKey);
                    behaviorObject.put("option", desktopKey);
                    categorySelectionsArray.put(behaviorObject);
                }
            }
            json.put("userID", SettingsEnum.SB_UUID.getString());
            json.put("isVip", SettingsEnum.SB_IS_VIP.getBoolean());
            json.put("serverAddress", SettingsEnum.SB_API_URL.getString());
            json.put("dontShowNotice", !SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean());
            json.put("showTimeWithSkips", SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean());
            json.put("minDuration", SettingsEnum.SB_MIN_DURATION.getFloat());
            json.put("trackViewCount", SettingsEnum.SB_COUNT_SKIPS.getBoolean());
            json.put("categorySelections", categorySelectionsArray);
            json.put("barTypes", barTypesObject);

            return json.toString(2);
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to export settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("sb_settings_export_failed"));
            return "";
        }
    }

    public static void loadFromSavedSettings() {
        ReVancedUtils.verifyOnMainThread();
        LogHelper.printDebug(() -> "updating SponsorBlockSettings");
        SharedPreferences preferences = SharedPrefHelper.getPreferences(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK);

        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            SponsorBlockViewController.hideSkipButton();
            SponsorBlockViewController.hideNewSegmentLayout();
            SegmentPlaybackController.setCurrentVideoId(null);
        }
        if (!SettingsEnum.SB_NEW_SEGMENT_ENABLED.getBoolean()) {
            SponsorBlockViewController.hideNewSegmentLayout();
        }

        // shield and voting button automatically show/hide themselves if feature is turned on/off

        SegmentCategory[] categories = SegmentCategory.valuesWithoutUnsubmitted();
        List<String> enabledCategories = new ArrayList<>(categories.length);
        for (SegmentCategory category : categories) {
            String categoryColor = preferences.getString(category.key + SEGMENT_CATEGORY_COLOR_PREFERENCE_KEY_SUFFIX, null);
            if (categoryColor != null) {
                category.setColor(Color.parseColor(categoryColor));
            }

            String value = preferences.getString(category.key, null);
            if (value != null) {
                CategoryBehaviour behavior = CategoryBehaviour.byStringKey(value);
                if (behavior == null) {
                    LogHelper.printException(() -> "Unknown behavior: " + value); // should never happen
                } else {
                    category.behaviour = behavior;
                }
            }
            if (category.behaviour != CategoryBehaviour.IGNORE) {
                enabledCategories.add(category.key);
            }
        }

        //"[%22sponsor%22,%22outro%22,%22music_offtopic%22,%22intro%22,%22selfpromo%22,%22interaction%22,%22preview%22]";
        if (enabledCategories.isEmpty())
            sponsorBlockAPIFetchCategories = "[]";
        else
            sponsorBlockAPIFetchCategories = "[%22" + TextUtils.join("%22,%22", enabledCategories) + "%22]";

        String uuid = SettingsEnum.SB_UUID.getString();
        if (uuid == null || uuid.length() == 0) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            SettingsEnum.SB_UUID.saveValue(uuid);
        }
    }
}
