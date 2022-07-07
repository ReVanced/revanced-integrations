package app.revanced.integrations.swipecontrols

import android.content.Context
import app.revanced.integrations.settings.SettingsEnum
import app.revanced.integrations.utils.PlayerType

/**
 * provider for configuration for volume and brightness swipe controls
 *
 * @param context the context to create in
 */
class SwipeControlsConfigurationProvider(
    private val context: Context
) {
//region swipe enable
    /**
     * should swipe controls be enabled? (global setting
     */
    val enableSwipeControls: Boolean
        get() = isFullscreenVideo && (enableVolumeControls || enableBrightnessControl)

    /**
     * should swipe controls for volume be enabled?
     */
    val enableVolumeControls: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.boolean

    /**
     * should swipe controls for volume be enabled?
     */
    val enableBrightnessControl: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.boolean

    /**
     * is the video player currently in fullscreen mode?
     */
    private val isFullscreenVideo: Boolean
        get() = PlayerType.current == PlayerType.WATCH_WHILE_FULLSCREEN
//endregion

//region behaviour adjustments
    /**
     * should press-to-swipe be enabled?
     */
    val shouldEnablePressToSwipe: Boolean
        get() = SettingsEnum.ENABLE_PRESS_TO_SWIPE_BOOLEAN.boolean

    /**
     * should the overlay enable haptic feedback?
     */
    val shouldEnableHapticFeedback: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_HAPTIC_FEEDBACK_BOOLEAN.boolean

    /**
     * how long the overlay should be shown on changes
     */
    val overlayShowTimeoutMillis: Long
        get() = SettingsEnum.SWIPE_OVERLAY_TIMEOUT_LONG.long

    /**
     * threshold for swipe detection
     * this may be called rapidly in onScroll, so we have to load it once and then leave it constant
     */
    val swipeMagnitudeThreshold: Float = SettingsEnum.SWIPE_MAGNITUDE_THRESHOLD_FLOAT.float
//endregion
}