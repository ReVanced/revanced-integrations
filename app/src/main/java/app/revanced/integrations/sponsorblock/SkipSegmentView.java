package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView.hideSkipButton;
import static app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView.showSkipButton;

import android.widget.Toast;

import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.ReVancedUtils;

public class SkipSegmentView {
    private static SponsorSegment lastNotifiedSegment;

    public static void show() {
        showSkipButton();
    }

    public static void hide() {
        hideSkipButton();
    }

    public static void notifySkipped(SponsorSegment segment) {
        if (segment == lastNotifiedSegment) return;
        lastNotifiedSegment = segment;
        String skipMessage = segment.category.getSkipMessage().toString();
        Toast.makeText(ReVancedUtils.getContext(), skipMessage, Toast.LENGTH_SHORT).show();
    }
}
