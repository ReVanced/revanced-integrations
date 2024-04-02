package app.revanced.integrations.youtube.settings.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

/**
 * Allows tapping to open the ReVanced website.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ReVancedAboutPreference extends Preference {

    private void init() {
        setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://revanced.app"));
            pref.getContext().startActivity(i);
            return false;
        });

        String summary = getSummary().toString()
                .replace("${PATCHES_RELEASE_VERSION}", Utils.getPatchesReleaseVersion())
                .replace("${PATCHES_RELEASE_DATE}", Utils.getPatchesReleaseDate());
        setSummary(summary);
    }

    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ReVancedAboutPreference(Context context) {
        super(context);
        init();
    }
}
