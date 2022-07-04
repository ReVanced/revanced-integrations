package app.revanced.integrations.fenster

/**
 * controls fenster feature enablement
 *
 * TODO: currently hard- coded settings values as i cannot get SettingsEnum to work...
 */
object FensterEnablement {

    /**
     * should fenster be enabled? (global setting)
     */
    val shouldEnableFenster: Boolean
        get() {
            return shouldEnableFensterVolumeControl || shouldEnableFensterBrightnessControl
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterVolumeControl: Boolean
        get() {
            return true //SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.boolean
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterBrightnessControl: Boolean
        get() {
            return true //SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.boolean
        }
}