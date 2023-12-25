package app.revanced.integrations.youtube.swipecontrols

import android.content.Context
import android.graphics.Color
import app.revanced.integrations.youtube.settings.Setting
import app.revanced.integrations.youtube.shared.PlayerType

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
     * should swipe controls be enabled? (global setting)
     */
    val enableSwipeControls: Boolean
        get() = isFullscreenVideo && (enableVolumeControls || enableBrightnessControl)

    /**
     * should swipe controls for volume be enabled?
     */
    val enableVolumeControls: Boolean
        get() = Setting.SWIPE_VOLUME.boolean

    /**
     * should swipe controls for volume be enabled?
     */
    val enableBrightnessControl: Boolean
        get() = Setting.SWIPE_BRIGHTNESS.boolean

    /**
     * is the video player currently in fullscreen mode?
     */
    private val isFullscreenVideo: Boolean
        get() = PlayerType.current == PlayerType.WATCH_WHILE_FULLSCREEN
//endregion

//region keys enable
    /**
     * should volume key controls be overwritten? (global setting)
     */
    val overwriteVolumeKeyControls: Boolean
        get() = isFullscreenVideo && enableVolumeControls
//endregion

//region gesture adjustments
    /**
     * should press-to-swipe be enabled?
     */
    val shouldEnablePressToSwipe: Boolean
        get() = Setting.SWIPE_PRESS_TO_ENGAGE.boolean

    /**
     * threshold for swipe detection
     * this may be called rapidly in onScroll, so we have to load it once and then leave it constant
     */
    val swipeMagnitudeThreshold: Int
        get() = Setting.SWIPE_MAGNITUDE_THRESHOLD.int
//endregion

//region overlay adjustments

    /**
     * should the overlay enable haptic feedback?
     */
    val shouldEnableHapticFeedback: Boolean
        get() = Setting.SWIPE_HAPTIC_FEEDBACK.boolean

    /**
     * how long the overlay should be shown on changes
     */
    val overlayShowTimeoutMillis: Long
        get() = Setting.SWIPE_OVERLAY_TIMEOUT.long

    /**
     * text size for the overlay, in sp
     */
    val overlayTextSize: Int
        get() = Setting.SWIPE_OVERLAY_TEXT_SIZE.int

    /**
     * get the background color for text on the overlay, as a color int
     */
    val overlayTextBackgroundColor: Int
        get() = Color.argb(Setting.SWIPE_OVERLAY_BACKGROUND_ALPHA.int, 0, 0, 0)

    /**
     * get the foreground color for text on the overlay, as a color int
     */
    val overlayForegroundColor: Int
        get() = Color.WHITE

//endregion

//region behaviour

    /**
     * should the brightness be saved and restored when exiting or entering fullscreen
     */
    val shouldSaveAndRestoreBrightness: Boolean
        get() = Setting.SWIPE_SAVE_AND_RESTORE_BRIGHTNESS.boolean

//endregion
}