package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Shows what thumbnails will be used based on the current settings.
 */
@SuppressWarnings("unused")
public class AlternativeThumbnailsStatusPreference extends Preference {

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        if (str.equals(SettingsEnum.ALT_THUMBNAILS_DEARROW.path)
                || str.equals(SettingsEnum.ALT_THUMBNAILS_STILLS.path)) {
            // Because this listener may run before the ReVanced settings fragment updates SettingsEnum,
            // this could show the prior config and not the current.
            //
            // Push this call to the end of the main run queue,
            // so all other listeners complete and then the settings are up to date.
            ReVancedUtils.runOnMainThread(this::updateUI);
        }
    };

    public AlternativeThumbnailsStatusPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public AlternativeThumbnailsStatusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public AlternativeThumbnailsStatusPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public AlternativeThumbnailsStatusPreference(Context context) {
        super(context);
    }

    private void addChangeListener() {
        SharedPrefCategory.YOUTUBE.preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private void removeChangeListener() {
        SharedPrefCategory.YOUTUBE.preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        updateUI();
        addChangeListener();
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        removeChangeListener();
    }

    private void updateUI() {
        LogHelper.printDebug(() -> "updateUI");
        final boolean usingVideoStills = SettingsEnum.ALT_THUMBNAILS_STILLS.getBoolean();
        final boolean usingDeArrow = SettingsEnum.ALT_THUMBNAILS_DEARROW.getBoolean();

        final String summaryTextKey;
        if (usingDeArrow && usingVideoStills) {
            summaryTextKey = "revanced_alt_thumbnails_about_status_dearrow_stills";
        } else if (usingDeArrow) {
            summaryTextKey = "revanced_alt_thumbnails_about_status_dearrow";
        } else if (usingVideoStills) {
            summaryTextKey = "revanced_alt_thumbnails_about_status_stills";
        } else {
            summaryTextKey = "revanced_alt_thumbnails_about_status_disabled";
        }

        setSummary(str(summaryTextKey));
    }
}
