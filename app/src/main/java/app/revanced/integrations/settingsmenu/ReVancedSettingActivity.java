package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.ResourceUtils.findView;
import static app.revanced.integrations.utils.ResourceUtils.identifier;

import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.libraries.social.licenses.LicenseActivity;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ResourceType;
import app.revanced.integrations.utils.ThemeHelper;

public class ReVancedSettingActivity {
    private static final String WHITE_THEME = "Theme.YouTube.Settings";
    private static final String DARK_THEME = "Theme.YouTube.Settings.Dark";

    public static void setTheme(LicenseActivity base) {
        final var theme = ThemeHelper.isDarkTheme() ? DARK_THEME : WHITE_THEME;
        LogHelper.debug(ReVancedSettingActivity.class, "Using theme: " + theme);

        base.setTheme(identifier(theme, ResourceType.STYLE));
    }

    public static void initializeSettings(LicenseActivity base) {
        base.setContentView(identifier("revanced_settings_with_toolbar", ResourceType.LAYOUT));

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
            getTextView(
                    findView(ReVancedSettingActivity.class, base, "toolbar")
            ).setText(preferenceIdentifier);
        } catch (Exception e) {
            LogHelper.printException(ReVancedSettingActivity.class, "Couldn't set Toolbar title", e);
        }

        base.getFragmentManager().beginTransaction().replace(identifier("revanced_settings_fragments", ResourceType.ID), preferenceFragment).commit();
    }


    public static <T extends View> T getView(Class<T> typeClass, ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt.getClass() == typeClass) {
                return typeClass.cast(childAt);
            }
        }
        return null;
    }

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        return getView(ImageButton.class, viewGroup);
    }

    public static TextView getTextView(ViewGroup viewGroup) {
        return getView(TextView.class, viewGroup);
    }
}
