package app.revanced.integrations.sponsorblock;

import android.content.Context;

import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;

public class NewSegmentHelperLayout {
    public static Context context;

    public static void show() {
        SponsorBlockView.showNewSegmentLayout();
    }

    public static void hide() {
        SponsorBlockView.hideNewSegmentLayout();
    }

    public static void toggle() {
        SponsorBlockView.toggleNewSegmentLayout();
    }
}
