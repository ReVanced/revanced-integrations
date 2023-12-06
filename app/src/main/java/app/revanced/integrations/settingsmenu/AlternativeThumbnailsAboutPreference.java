package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Shows what thumbnails will be used based on the current settings.
 */
@SuppressWarnings("unused")
public class AlternativeThumbnailsAboutPreference extends Preference {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private void init() {
        setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://dearrow.ajay.app"));
            pref.getContext().startActivity(i);
            return false;
        });

        updateUI();

        listener = (sharedPreferences, str) -> {
            // Because this listener may run before the ReVanced settings fragment updates SettingsEnum,
            // this could show the prior config and not the current.
            //
            // Push this call to the end of the main run queue,
            // so all other listeners complete and then the settings are up to date.
            ReVancedUtils.runOnMainThread(this::updateUI);
        };
        SharedPrefCategory.YOUTUBE.preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private void removeChangeListener() {
        SharedPrefCategory.YOUTUBE.preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public AlternativeThumbnailsAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    public AlternativeThumbnailsAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public AlternativeThumbnailsAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public AlternativeThumbnailsAboutPreference(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();

        removeChangeListener();
    }

    private void updateUI() {
        final boolean usingVideoStills = SettingsEnum.ALT_THUMBNAIL_STILLS.getBoolean();
        final boolean usingDeArrow = SettingsEnum.ALT_THUMBNAIL_DEARROW.getBoolean();

        final String summaryText;
        boolean isSelectable = false;
        if (usingDeArrow) {
            isSelectable = true;
            String summaryKey = "revanced_alt_thumbnail_about_summary_dearrow";
            String additionalSummaryText = str(usingVideoStills
                    ? "revanced_alt_thumbnail_about_summary_dearrow_fallback_stills"
                    : "revanced_alt_thumbnail_about_summary_dearrow_fallback_none");
            String webLinkDescription = str("revanced_alt_thumbnail_about_summary_link_text");
            summaryText = str(summaryKey, additionalSummaryText, webLinkDescription);
        } else if (usingVideoStills) {
            summaryText = str("revanced_alt_thumbnail_about_summary_stills");
        } else {
            summaryText = str("revanced_alt_thumbnail_about_summary_disabled");
        }

        setSummary(summaryText);
        setSelectable(isSelectable);
    }
}
