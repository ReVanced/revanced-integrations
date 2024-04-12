package app.revanced.integrations.youtube.swipecontrols.controller

import android.app.Activity
import android.view.WindowManager
import app.revanced.integrations.shared.Utils
import app.revanced.integrations.youtube.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.integrations.youtube.swipecontrols.misc.clamp

/**
 * controller to adjust the screen brightness level
 *
 * @param host the host activity of which the brightness is adjusted
 */
class ScreenBrightnessController(
    private val host: Activity,
    val config: SwipeControlsConfigurationProvider,
) {

    /**
     * the current screen brightness in percent, ranging from 0.0 to 100.0
     */
    var screenBrightness: Double
        get() = rawScreenBrightness * 100.0
        set(value) {
            rawScreenBrightness = (value.toFloat() / 100f).clamp(0f, 1f)
        }

    /**
     * restore the screen brightness to the default device brightness
     */
    fun restoreDefaultBrightness() {
        rawScreenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    /**
     * is the screen brightness set to device- default?
     */
    val isDefaultBrightness
        get() = (rawScreenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)

    fun save() {
        if (config.savedScreenBrightnessValue == -1f) {
            config.savedScreenBrightnessValue = rawScreenBrightness
        }
    }

    /**
     * restore the screen brightness from settings saved using [save]
     */
    fun restore() {
        if (config.savedScreenBrightnessValue != -1f) {
            rawScreenBrightness = config.savedScreenBrightnessValue
            config.savedScreenBrightnessValue = -1f
        } else {
            // here need to override -1f, because in some cases the brightness is not restored after force closing app
            config.savedScreenBrightnessValue = -1f
        }
    }

    /**
     * wrapper for the raw screen brightness in [WindowManager.LayoutParams.screenBrightness]
     */
    var rawScreenBrightness: Float
        get() = host.window.attributes.screenBrightness
        private set(value) {
            val attr = host.window.attributes
            attr.screenBrightness = value
            host.window.attributes = attr
        }
}