package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.ReVancedUtils.getChildView;
import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdentifier;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ThemeHelper;

public class ReVancedSettingActivity extends Activity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            ThemeHelper.setActivityTheme(this);
            setContentView(getResourceIdentifier("revanced_settings_with_toolbar", "layout"));
            setBackButton();

            PreferenceFragment fragment;
            String toolbarTitleResourceName;
            String dataString = getIntent().getDataString();
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
                    fragment = new ReVancedSettingsFragment();
                    break;
                default:
                    LogHelper.printException(() -> "Unknown setting: " + dataString);
                    return;
            }

            setToolbarTitle(toolbarTitleResourceName);
            getFragmentManager()
                    .beginTransaction()
                    .replace(getResourceIdentifier("revanced_settings_fragments", "id"), fragment)
                    .commit();

        } catch (Exception ex) {
            LogHelper.printException(() -> "onCreate failure", ex);
        }
    }

    private void setToolbarTitle(String toolbarTitleResourceName) {
        ViewGroup toolbar = findViewById(getToolbarResourceId());
        TextView toolbarTextView = getChildView(toolbar, view -> view instanceof TextView);
        toolbarTextView.setText(getResourceIdentifier(toolbarTitleResourceName, "string"));
    }

    private void setBackButton() {
        ViewGroup toolbar = findViewById(getToolbarResourceId());
        ImageButton imageButton = getChildView(toolbar, view -> view instanceof ImageButton);
        final int backButtonResource = getResourceIdentifier(ThemeHelper.isDarkTheme()
                        ? "yt_outline_arrow_left_white_24"
                        : "yt_outline_arrow_left_black_24",
                "drawable");
        imageButton.setImageDrawable(getResources().getDrawable(backButtonResource));
        imageButton.setOnClickListener(view -> onBackPressed());
    }

    private static int getToolbarResourceId() {
        final int toolbarResourceId = getResourceIdentifier("revanced_toolbar", "id");
        if (toolbarResourceId == 0) {
            throw new IllegalStateException("Could not find back button resource");
        }
        return toolbarResourceId;
    }

}
