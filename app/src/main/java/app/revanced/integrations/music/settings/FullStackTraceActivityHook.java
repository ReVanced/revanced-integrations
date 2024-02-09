package app.revanced.integrations.music.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import app.revanced.integrations.music.settings.preference.ReVancedPreferenceFragment;
import app.revanced.integrations.shared.Logger;

import java.util.Objects;

import static app.revanced.integrations.shared.Utils.getChildView;
import static app.revanced.integrations.shared.Utils.getResourceIdentifier;

/**
 * Hooks FullStackTraceActivityHook.
 * <p>
 * This class is responsible for injecting our own fragment by replacing the FullStackTraceActivityHook.
 */
@SuppressWarnings("unused")
public class FullStackTraceActivityHook {

    /**
     * Injection point.
     * <p>
     * Hooks FullStackTraceActivityHook#onCreate in order to inject our own fragment.
     */
    public static void initialize(Activity fullStackTraceActivityHook) {
        try {
            // ThemeHelper.setActivityTheme(fullStackTraceActivityHook);
            fullStackTraceActivityHook.setContentView(
                    getResourceIdentifier("revanced_settings_with_toolbar", "layout"));
            setBackButton(fullStackTraceActivityHook);

            PreferenceFragment fragment;
            String toolbarTitleResourceName;
            String dataString = fullStackTraceActivityHook.getIntent().getDataString();
            switch (dataString) {
                case "revanced_settings_intent":
                    toolbarTitleResourceName = "revanced_settings_title";
                    fragment = new ReVancedPreferenceFragment();
                    break;
                default:
                    Logger.printException(() -> "Unknown setting: " + dataString);
                    return;
            }

            setToolbarTitle(fullStackTraceActivityHook, toolbarTitleResourceName);
            fullStackTraceActivityHook.getFragmentManager()
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
        final int backButtonResource = getResourceIdentifier(false // ThemeHelper.isDarkTheme()
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
