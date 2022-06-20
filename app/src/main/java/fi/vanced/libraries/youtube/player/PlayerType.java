package fi.vanced.libraries.youtube.player;

import fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;

public class PlayerType {
    public static void playerTypeChanged(String playerType) {
        SponsorBlockView.playerTypeChanged(playerType);
        SponsorBlockUtils.playerTypeChanged(playerType);
    }
}
