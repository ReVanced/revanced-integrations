package com.google.android.apps.youtube.app.settings.videoquality;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import app.revanced.integrations.settingsmenu.ReVancedSettingsFragment;
import app.revanced.integrations.settingsmenu.ReturnYouTubeDislikeSettingsFragment;
import app.revanced.integrations.settingsmenu.SponsorBlockSettingsFragment;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

// Hook a dummy Activity to make the Back button work
public class VideoQualitySettingsActivity extends Activity {

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        final var whiteTheme = "Theme.YouTube.Settings";
        final var darkTheme = "Theme.YouTube.Settings.Dark";

        final var theme = ThemeHelper.isDarkTheme() ? darkTheme : whiteTheme;

        LogHelper.printDebug(() -> "Using theme: " + theme);
        setTheme(getIdentifier(theme, "style"));

        super.onCreate(bundle);
        setContentView(getIdentifier("revanced_settings_with_toolbar", "layout"));
        initImageButton();

        int fragment = getIdentifier("revanced_settings_fragments", "id");

        String dataString = getIntent().getDataString();
        if (dataString.equalsIgnoreCase("sponsorblock_settings")) {
            trySetTitle(getIdentifier("sb_settings", "string"));
            getFragmentManager()
                    .beginTransaction()
                    .replace(fragment, new SponsorBlockSettingsFragment())
                    .commit();
        } else if (dataString.equalsIgnoreCase("ryd_settings")) {
            trySetTitle(getIdentifier("revanced_ryd_settings_title", "string"));
            getFragmentManager()
                    .beginTransaction()
                    .replace(fragment, new ReturnYouTubeDislikeSettingsFragment())
                    .commit();
        } else {
            trySetTitle(getIdentifier("revanced_settings", "string"));
            getFragmentManager()
                    .beginTransaction()
                    .replace(fragment, new ReVancedSettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static ImageButton getImageButton(ViewGroup viewGroup) {
        if (viewGroup == null)  return null;
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
        if (viewGroup == null) return null;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof TextView) {
                return (TextView) childAt;
            }
        }
        return null;
    }

    private void trySetTitle(int i) {
        try {
            getTextView((ViewGroup) findViewById(getIdentifier("toolbar", "id"))).setText(i);
        } catch (Exception e) {
            LogHelper.printException(() -> ("Couldn't set Toolbar title"), e);
        }
    }

    private static int getIdentifier(String name, String defType) {
        Context appContext = ReVancedUtils.getContext();
        assert appContext != null;
        return appContext.getResources().getIdentifier(name, defType, appContext.getPackageName());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initImageButton() {
        final var whiteArrow = "yt_outline_arrow_left_white_24";
        final var darkArrow = "yt_outline_arrow_left_black_24";

        final var arrow = ThemeHelper.isDarkTheme() ? whiteArrow : darkArrow;

        try {
            ImageButton imageButton = getImageButton((ViewGroup) findViewById(getIdentifier("toolbar", "id")));
            imageButton.setOnClickListener(view -> VideoQualitySettingsActivity.this.onBackPressed());
            imageButton.setImageDrawable(getResources().getDrawable(getIdentifier(arrow, "drawable")));
        } catch (Exception e) {
            LogHelper.printException(() -> ("Couldn't set Toolbar click handler"), e);
        }
    }
}