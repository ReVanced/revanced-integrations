package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.CategoryBehaviour;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentCategory;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.Html;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.objects.EditTextListPreference;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class SponsorBlockSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final List<Preference> preferencesToDisableWhenSBDisabled = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK.getName());
        preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = preferenceManager.createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        SponsorBlockSettings.update();

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setChecked(SettingsEnum.SB_ENABLED.getBoolean());
            preference.setTitle(str("sb_enable_sb"));
            preference.setSummary(str("sb_enable_sb_sum"));
            preference.setOnPreferenceChangeListener((preference1, o) -> {
                Boolean newValue = (Boolean) o;
                enableCategoriesIfNeeded(newValue);
                SettingsEnum.SB_ENABLED.saveValue(newValue);
                return true;
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setChecked(SettingsEnum.SB_NEW_SEGMENT_ENABLED.getBoolean());
            preference.setTitle(str("sb_enable_add_segment"));
            preference.setSummary(str("sb_enable_add_segment_sum"));
            preferencesToDisableWhenSBDisabled.add(preference);
            preference.setOnPreferenceChangeListener((preference1, o) -> {
                Boolean newValue = (Boolean) o;
                if (newValue && !SettingsEnum.SB_SEEN_GUIDELINES.getBoolean()) {
                    new AlertDialog.Builder(preference1.getContext())
                            .setTitle(str("sb_guidelines_popup_title"))
                            .setMessage(str("sb_guidelines_popup_content"))
                            .setNegativeButton(str("sb_guidelines_popup_already_read"), null)
                            .setPositiveButton(str("sb_guidelines_popup_open"), (dialogInterface, i) -> openGuidelines())
                            .show();
                }
                SettingsEnum.SB_NEW_SEGMENT_ENABLED.saveValue(newValue);
                return true;
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setTitle(str("sb_enable_voting"));
            preference.setSummary(str("sb_enable_voting_sum"));
            preference.setChecked(SettingsEnum.SB_VOTING_ENABLED.getBoolean());
            preferencesToDisableWhenSBDisabled.add(preference);
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_VOTING_ENABLED.saveValue(newValue);
                return true;
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setTitle(str("sb_enable_compact_skip_button"));
            preference.setSummary(str("sb_enable_compact_skip_button_sum"));
            preference.setChecked(SettingsEnum.SB_USE_COMPACT_SKIPBUTTON.getBoolean());
            preferencesToDisableWhenSBDisabled.add(preference);
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_USE_COMPACT_SKIPBUTTON.saveValue(newValue);
                return true;
            });
        }

        addGeneralCategory(context, preferenceScreen);
        addSegmentsCategory(context, preferenceScreen);
        addStatsCategory(context, preferenceScreen);
        addAboutCategory(context, preferenceScreen);

        enableCategoriesIfNeeded(SettingsEnum.SB_ENABLED.getBoolean());
    }

    private void openGuidelines() {
        SettingsEnum.SB_SEEN_GUIDELINES.saveValue(true);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
        getActivity().startActivity(intent);
    }

    private void enableCategoriesIfNeeded(boolean value) {
        for (Preference preference : preferencesToDisableWhenSBDisabled)
            preference.setEnabled(value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void addSegmentsCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(context);
        screen.addPreference(preferenceCategory);
        preferencesToDisableWhenSBDisabled.add(preferenceCategory);
        preferenceCategory.setTitle(str("sb_diff_segments"));

        CategoryBehaviour[] behaviours = CategoryBehaviour.values();
        String[] behaviorNames = new String[behaviours.length];
        String[] behaviorKeys = new String[behaviours.length];
        for (int i = 0, length = behaviours.length; i < length; i++) {
            CategoryBehaviour behaviour = behaviours[i];
            behaviorNames[i] = behaviour.name.toString();
            behaviorKeys[i] = behaviour.key;
        }

        for (SegmentCategory category : SegmentCategory.valuesWithoutUnsubmitted()) {
            EditTextListPreference listPreference = new EditTextListPreference(context);
            listPreference.setTitle(category.getTitleWithDot());
            listPreference.setSummary(category.description.toString());
            listPreference.setKey(category.key);
            listPreference.setDefaultValue(category.behaviour.key);
            listPreference.setEntries(behaviorNames);
            listPreference.setEntryValues(behaviorKeys);

            preferenceCategory.addPreference(listPreference);
        }

        Preference colorPreference = new Preference(context); // TODO remove this after the next major update
        screen.addPreference(colorPreference);
        colorPreference.setTitle(str("sb_color_change"));
        colorPreference.setSummary(str("sb_color_change_sum"));
        colorPreference.setSelectable(false);
        preferencesToDisableWhenSBDisabled.add(colorPreference);
    }

    private void addStatsCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("sb_stats"));
        preferencesToDisableWhenSBDisabled.add(category);

        Preference preference = new Preference(context);
        preference.setSelectable(false);
        category.addPreference(preference);
        if (SettingsEnum.SB_ENABLED.getBoolean()) {
            preference.setTitle(str("sb_stats_loading"));
            ReVancedUtils.runOnBackgroundThread(() -> SBRequester.retrieveUserStats(category, preference));
        } else {
            preference.setTitle(str("sb_stats_sb_disabled"));
        }
    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("sb_about"));

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("sb_about_api"));
            preference.setSummary(str("sb_about_api_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://sponsor.ajay.app"));
                preference1.getContext().startActivity(i);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("sb_about_madeby"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                preference.setSingleLineTitle(false);
            }
            preference.setSelectable(false);
        }

    }

    private void addGeneralCategory(final Context context, PreferenceScreen screen) {
        final PreferenceCategory category = new PreferenceCategory(context);
        preferencesToDisableWhenSBDisabled.add(category);
        screen.addPreference(category);
        category.setTitle(str("sb_general"));

        {
            Preference preference = new Preference(context);
            preference.setTitle(str("sb_guidelines_preference_title"));
            preference.setSummary(str("sb_guidelines_preference_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                openGuidelines();
                return false;
            });
            screen.addPreference(preference);
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preference.setTitle(str("sb_general_skiptoast"));
            preference.setSummary(str("sb_general_skiptoast_sum"));
            preference.setChecked(SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean());
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.saveValue(newValue);
                return true;
            });
            preference.setOnPreferenceClickListener(preference12 -> {
                ReVancedUtils.showToastShort(str("skipped_sponsor"));
                return false;
            });
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preference.setTitle(str("sb_general_skipcount"));
            preference.setSummary(str("sb_general_skipcount_sum"));
            preference.setChecked(SettingsEnum.SB_COUNT_SKIPS.getBoolean());
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_COUNT_SKIPS.saveValue(newValue);
                return true;
            });
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preference.setTitle(str("sb_general_time_without"));
            preference.setSummary(str("sb_general_time_without_sum"));
            preference.setChecked(SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean());
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.saveValue(newValue);
                return true;
            });

            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            preference.setTitle(str("sb_general_adjusting"));
            preference.setSummary(str("sb_general_adjusting_sum"));
            preference.setText(String.valueOf(SettingsEnum.SB_ADJUST_NEW_SEGMENT_STEP.getInt()));
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_ADJUST_NEW_SEGMENT_STEP.saveValue(Integer.valueOf(newValue.toString()));
                return true;
            });
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            preference.setTitle(str("sb_general_min_duration"));
            preference.setSummary(str("sb_general_min_duration_sum"));
            preference.setText(String.valueOf(SettingsEnum.SB_MIN_DURATION.getFloat()));
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_MIN_DURATION.saveValue(Float.valueOf(newValue.toString()));
                return true;
            });
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.setTitle(str("sb_general_uuid"));
            preference.setSummary(str("sb_general_uuid_sum"));
            preference.setText(SettingsEnum.SB_UUID.getString());
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SettingsEnum.SB_UUID.saveValue(newValue.toString());
                return true;
            });

            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            Preference preference = new Preference(context);
            String title = str("sb_general_api_url");
            preference.setTitle(title);
            preference.setSummary(Html.fromHtml(str("sb_general_api_url_sum")));
            preference.setOnPreferenceClickListener(preference1 -> {
                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                editText.setText(SettingsEnum.SB_API_URL.getString());

                APIURLChangeListener urlListener = new APIURLChangeListener(editText);
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(str("reset"), urlListener)
                        .setPositiveButton(android.R.string.ok, urlListener)
                        .show();
                return true;
            });

            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.setTitle(str("sb_settings_ie"));
            preference.setSummary(str("sb_settings_ie_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                preference.getEditText().setText(SponsorBlockUtils.exportSettings());
                return false;
            });
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SponsorBlockUtils.importSettings((String) newValue);
                return false;
            });
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SponsorBlockSettings.update();
    }

    private static class APIURLChangeListener implements DialogInterface.OnClickListener {
        private final EditText editText;

        public APIURLChangeListener(EditText editText) {
            this.editText = Objects.requireNonNull(editText);
        }

        @Override
        public void onClick(DialogInterface dialog, int buttonPressed) {
            if (buttonPressed == DialogInterface.BUTTON_NEUTRAL) {
                SettingsEnum.SB_API_URL.saveValue(SettingsEnum.SB_API_URL.getDefaultValue());
                ReVancedUtils.showToastLong(str("sb_api_url_reset"));
            } else if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                String textAsString = editText.getText().toString();
                if (!Patterns.WEB_URL.matcher(textAsString).matches()) {
                    ReVancedUtils.showToastLong(str("sb_api_url_invalid"));
                } else if (!textAsString.equals(SettingsEnum.SB_API_URL.getString())) {
                    SettingsEnum.SB_API_URL.saveValue(textAsString);
                    ReVancedUtils.showToastLong(str("sb_api_url_changed"));
                }
            }
        }
    }

}
