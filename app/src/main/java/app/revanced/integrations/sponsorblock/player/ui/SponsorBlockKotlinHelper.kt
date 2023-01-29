package app.revanced.integrations.sponsorblock.player.ui

import app.revanced.integrations.shared.PlayerType

/**
 * Used to bridge from java code to other Kotlin code
 */
class SponsorBlockKotlinHelper {
    companion object {
        @JvmStatic
        fun registerSponsorBlockViewPlayerTypeChangeListener() {
            PlayerType.onChange += this::onPlayerTypeChanged
        }

        private fun onPlayerTypeChanged(type: PlayerType) {
            SponsorBlockView.playerTypeChanged(type)
        }
    }
}