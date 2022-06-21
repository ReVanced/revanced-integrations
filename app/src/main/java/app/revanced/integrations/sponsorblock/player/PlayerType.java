package app.revanced.integrations.sponsorblock.player;

import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;

public class PlayerType {
    public static void playerTypeChanged(String playerType) {
        SponsorBlockView.playerTypeChanged(playerType);
        SponsorBlockUtils.playerTypeChanged(playerType);
    }
}
