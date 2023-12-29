package app.revanced.integrations.youtube.settingsmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.ThemeHelper;

import java.util.Objects;

import static app.revanced.integrations.shared.Utils.getChildView;
import static app.revanced.integrations.shared.Utils.getResourceIdentifier;

@SuppressWarnings("unused")
public class ReVancedSettingActivity {

    /**
     * Injection point.
     */
    public static void initializeSettings(Activity licenseActivity) {
        try {
            ThemeHelper.setActivityTheme(licenseActivity);
            licenseActivity.setContentView(
                    getResourceIdentifier("revanced_settings_with_toolbar", "layout"));
            setBackButton(licenseActivity);

            android.preference.PreferenceFragment fragment;
            String toolbarTitleResourceName;
            String dataString = licenseActivity.getIntent().getDataString();
            switch (dataString) {
                case "sponsorblock_settings":
                    toolbarTitleResourceName = "revanced_sponsorblock_settings_title";
                    fragment = new SponsorBlockSettingsFragment();
                    break;
                case "ryd_settings":
                    toolbarTitleResourceName = "revanced_ryd_settings_title";
                    fragment = new ReturnYouTubeDislikeSettingsFragment();
                    break;
                case "revanced_settings":
                    toolbarTitleResourceName = "revanced_settings_title";
                    fragment = new YouTubePreferenceFragment();
                    break;
                default:
                    Logger.printException(() -> "Unknown setting: " + dataString);
                    return;
            }

            setToolbarTitle(licenseActivity, toolbarTitleResourceName);
            licenseActivity.getFragmentManager()
                    .beginTransaction()
                    .replace(getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate failure", ex);
        }
    }

    private static void setToolbarTitle(Activity activity, String toolbarTitleResourceName) {
        ViewGroup toolbar = activity.findViewById(getToolbarResourceId());
        TextView toolbarTextView = Objects.requireNonNull(getChildView(toolbar, view -> view instanceof TextView));
        toolbarTextView.setText(getResourceIdentifier(toolbarTitleResourceName, "string"));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void setBackButton(Activity activity) {
        ViewGroup toolbar = activity.findViewById(getToolbarResourceId());
        ImageButton imageButton = Objects.requireNonNull(getChildView(toolbar, view -> view instanceof ImageButton));
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        imageButton.setImageDrawable(activity.getResources().getDrawable(backButtonResource));
        imageButton.setOnClickListener(view -> activity.onBackPressed());
    }

    private static int getToolbarResourceId() {
        final int toolbarResourceId = getResourceIdentifier("revanced_toolbar", "id");
        if (toolbarResourceId == 0) {
            throw new IllegalStateException("Could not find back button resource");
        }
        return toolbarResourceId;
    }

}
