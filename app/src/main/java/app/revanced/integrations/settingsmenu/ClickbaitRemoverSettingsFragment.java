package app.revanced.integrations.settingsmenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import app.revanced.integrations.patches.ClickbaitRemoverPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

@SuppressWarnings("deprecation")
public class ClickbaitRemoverSettingsFragment extends PreferenceFragment {

    // TODO: move strings to ReVanced Patches

    private SwitchPreference deArrowEnabled;
    private SwitchPreference altThumbnailsEnabled;
    private ListPreference altThumbnailsType;
    private SwitchPreference altThumbnailsFast;

    private void updateUI() {
        try {
            deArrowEnabled.setChecked(SettingsEnum.CLICKBAIT_DEARROW.getBoolean());

            altThumbnailsEnabled.setChecked(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL.getBoolean());
            altThumbnailsType.setEnabled(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.isAvailable());
            altThumbnailsType.setValue(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.getObjectValue().toString());
            altThumbnailsType.setSummary(altThumbnailsType.getEntry());
            altThumbnailsFast.setEnabled(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_FAST_QUALITY.isAvailable());
            altThumbnailsFast.setChecked(SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_FAST_QUALITY.getBoolean());
        } catch (Exception ex) {
            LogHelper.printException(() -> "update settings UI failure", ex);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Activity context = this.getActivity();

            PreferenceManager preferenceManager = getPreferenceManager();
            PreferenceScreen preferenceScreen = preferenceManager.createPreferenceScreen(context);
            setPreferenceScreen(preferenceScreen);

            addDeArrowCategory(context, preferenceScreen);

            // Add some vertical padding.
            preferenceScreen.addPreference(new PreferenceCategory(context));

            addAltThumbnailsCategory(context, preferenceScreen);

            updateUI();
        } catch (Exception ex) {
            LogHelper.printException(() -> "onCreate failure", ex);
        }
    }

    private void addDeArrowCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        category.setTitle("DeArrow");
        screen.addPreference(category);

        deArrowEnabled = new SwitchPreference(context);
        deArrowEnabled.setTitle("Use DeArrow thumbnails");
        deArrowEnabled.setSummaryOn("Using DeArrow crowd sourced thumbnails.  If DeArrow is not available for a video, then the original thumbnail is shown");
        deArrowEnabled.setSummaryOff("DeArrow is not used");
        deArrowEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
            // To keep things simpler, for now only allow a single strategy to be enabled.
            if (((Boolean) newValue)) {
                SettingsEnum.CLICKBAIT_ALT_THUMBNAIL.saveValue(false);
                ClickbaitRemoverPatch.resetAPISuccessRate();
            }
            SettingsEnum.CLICKBAIT_DEARROW.saveValue(newValue);
            updateUI();
            return true;
        });
        category.addPreference(deArrowEnabled);

        Preference about = new Preference(context);
        category.addPreference(about);
        about.setTitle("dearrow.ajay.app");
        about.setSummary("DeArrow is an API for crowd sourcing better YouTube thumbnails. "
                + "The goal is to make thumbnails more sensible. No more arrows, ridiculous faces, or clickbait."
                + "\n\nTap here to learn more and see downloads for other platforms");
        about.setOnPreferenceClickListener(preference1 -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://dearrow.ajay.app"));
            preference1.getContext().startActivity(i);
            return false;
        });
    }

    private void addAltThumbnailsCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        category.setTitle("Alternate YouTube thumbnails");
        screen.addPreference(category);

        altThumbnailsEnabled = new SwitchPreference(context);
        altThumbnailsEnabled.setTitle("Use alternate YouTube thumbnails");
        altThumbnailsEnabled.setSummaryOn("YouTube video stills used as thumbnails");
        altThumbnailsEnabled.setSummaryOff("YouTube video stills not used as thumbnails");
        altThumbnailsEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
            if (((Boolean) newValue)) {
                SettingsEnum.CLICKBAIT_DEARROW.saveValue(false);
            }
            SettingsEnum.CLICKBAIT_ALT_THUMBNAIL.saveValue(newValue);
            updateUI();
            return true;
        });
        category.addPreference(altThumbnailsEnabled);

        altThumbnailsType = new ListPreference(context);
        altThumbnailsType.setTitle("Alternate thumbnail type");
        altThumbnailsType.setEntries(new String[]{
                "Beginning of video",
                "Middle of video",
                "End of video"});
        altThumbnailsType.setEntryValues(new String[]{"1", "2", "3"});
        altThumbnailsType.setOnPreferenceChangeListener((preference1, newValue) -> {
            SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_TYPE.saveValue(Integer.parseInt((String) newValue));
            updateUI();
            return true;
        });
        category.addPreference(altThumbnailsType);

        altThumbnailsFast = new SwitchPreference(context);
        altThumbnailsFast.setTitle("Use fast alt thumbnails");
        altThumbnailsFast.setSummaryOn("Using medium quality alternate thumbnails."
                + "  Thumbnails will load faster, but live streams, unreleased, or very old videos may show blank thumbnails");
        altThumbnailsFast.setSummaryOff("Using higher quality alternate thumbnails");
        altThumbnailsFast.setOnPreferenceChangeListener((preference1, newValue) -> {
            SettingsEnum.CLICKBAIT_ALT_THUMBNAIL_FAST_QUALITY.saveValue(newValue);
            updateUI();
            return true;
        });
        category.addPreference(altThumbnailsFast);

        Preference about = new Preference(context);
        category.addPreference(about);
        about.setSummary("Alternate thumbnails are provided by YouTube,"
                + " where the thumbnails are still images of the beginning/middle/end of each video."
                + "  No external API is used, as these images are built into YouTube");
    }

}
