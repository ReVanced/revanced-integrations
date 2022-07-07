package app.revanced.integrations.swipecontrols

import android.content.Context
import app.revanced.integrations.settings.SettingsEnum

/**
 * provider for configuration for fenster swipe controls
 *
 * @param context the context to create in
 */
class SwipeControlsConfigurationProvider(
    private val context: Context
) {
    /**
     * should fenster be enabled? (global setting
     */
    val shouldEnableFenster: Boolean
        get() = isFullscreenVideo && (shouldEnableFensterVolumeControl || shouldEnableFensterBrightnessControl)

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterVolumeControl: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.boolean


    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterBrightnessControl: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.boolean

    /**
     * should press-to-swipe be enabled?
     */
    val shouldEnablePressToSwipe: Boolean
        get() = false

    /**
     * is the video player currently in fullscreen mode?
     */
    private val isFullscreenVideo: Boolean
        get() = WatchWhilePlayerType.current == WatchWhilePlayerType.WATCH_WHILE_FULLSCREEN
}