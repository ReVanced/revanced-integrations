package app.revanced.integrations.settingsmenu;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.libraries.social.LicenseActivity;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

public class ReVancedSettingActivity extends LicenseActivity {

    public static void setTheme(LicenseActivity base) {
        var currentTheme = ThemeHelper.isDarkTheme();
        if (currentTheme) {
            LogHelper.debug(ReVancedSettingActivity.class, "set Theme.YouTube.Settings.Dark");
            base.setTheme(getIdentifier("Theme.YouTube.Settings.Dark", "style"));
        } else {
            LogHelper.debug(ReVancedSettingActivity.class, "set Theme.YouTube.Settings");
            base.setTheme(getIdentifier("Theme.YouTube.Settings", "style"));
        }
    }

    public static void initializeSettings(LicenseActivity base) {
        base.setContentView(getIdentifier("xsettings_with_toolbar", "layout"));

        try {
            ImageButton imageButton = getImageButton(base.findViewById(getIdentifier("toolbar", "id")));
            imageButton.setOnClickListener(view -> base.onBackPressed());
            imageButton.setImageDrawable(base.getResources().getDrawable(getIdentifier(ThemeHelper.isDarkTheme() ? "quantum_ic_arrow_back_white_24" : "quantum_ic_arrow_back_grey600_24", "drawable"), null));
        } catch (Exception e) {
            LogHelper.printException(ReVancedSettingActivity.class, "Couldn't set Toolbar click handler", e);
        }

        PreferenceFragment preferenceFragment;
        String preferenceIdentifier;

        String dataString = base.getIntent().getDataString();
        if (dataString.equalsIgnoreCase("sponsorblock_settings")) {
            preferenceIdentifier = "sb_settings";
            preferenceFragment = new SponsorBlockSettingsFragment();
        } else if (dataString.equalsIgnoreCase("ryd_settings")) {
            preferenceIdentifier = "revanced_ryd_settings_title";
            preferenceFragment = new ReturnYouTubeDislikeSettingsFragment();
        } else {
            preferenceIdentifier = "revanced_settings";
            preferenceFragment = new ReVancedSettingsFragment();
        }

        try {
            var resourceIdentifier = getIdentifier(preferenceIdentifier, "string");
            getTextView(base.findViewById(getIdentifier("toolbar", "id"))).setText(resourceIdentifier);
        } catch (Exception e) {
            LogHelper.printException(ReVancedSettingActivity.class, "Couldn't set Toolbar title", e);
        }

        base.getFragmentManager().beginTransaction().replace(getIdentifier("xsettings_fragments", "id"), preferenceFragment).commit();
    }

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ImageButton) {
                return (ImageButton) childAt;
            }
        }
        return null;
    }

    public static TextView getTextView(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof TextView) {
                return (TextView) childAt;
            }
        }
        return null;
    }

    private static int getIdentifier(String str, String str2) {
        Context appContext = ReVancedUtils.getContext();
        assert appContext != null;
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }
}
