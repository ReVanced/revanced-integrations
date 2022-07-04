package app.revanced.integrations.fenster

/**
 * controls fenster feature enablement
 *
 * TODO: currently hardcoded to enable everything. When user preferences are introduced, they can be inserted here
 */
object FensterEnablement {

    /**
     * should fenster be enabled? (global setting)
     */
    val shouldEnableFenster: Boolean
        get() {
            return true
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterVolumeControl: Boolean
        get() {
            return true
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterBrightnessControl: Boolean
        get() {
            return true
        }

}