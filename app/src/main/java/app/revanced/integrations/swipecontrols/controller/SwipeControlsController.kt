package app.revanced.integrations.swipecontrols.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.integrations.swipecontrols.controller.gesture.SwipeGestureController
import app.revanced.integrations.swipecontrols.misc.Rectangle
import app.revanced.integrations.swipecontrols.misc.SwipeControlsOverlay
import app.revanced.integrations.swipecontrols.misc.SwipeZonesHelper
import app.revanced.integrations.swipecontrols.views.SwipeControlsOverlayLayout
import app.revanced.integrations.swipecontrols.views.TouchThiefLayout
import app.revanced.integrations.swipecontrols.views.injectTouchThief
import app.revanced.integrations.utils.LogHelper

/**
 * The main controller for 'FensterV2' Swipe controls
 */
//TODO options to remove this supression?
@SuppressLint("StaticFieldLeak")
object SwipeControlsController {

    /**
     * current instance of [AudioVolumeController]
     */
    var audio: AudioVolumeController? = null
        private set

    /**
     * current instance of [ScreenBrightnessController]
     */
    var screen: ScreenBrightnessController? = null
        private set

    /**
     * current instance of [SwipeControlsConfigurationProvider]
     */
    var config: SwipeControlsConfigurationProvider? = null
        private set

    /**
     * current instance of [SwipeControlsOverlayLayout]
     */
    var overlay: SwipeControlsOverlay? = null
        private set

    private var hostActivity: Activity? = null
    private var gesture: SwipeGestureController? = null
    private var thief: TouchThiefLayout? = null

    /**
     * Initializes the controller and touch thief
     *
     * @param host the activity that hosts the controller
     */
    fun initialize(host: Activity) {
        if (hostActivity != null) {
            if (host == hostActivity) {
                // function was called twice, ignore the call
                LogHelper.debug(
                    this.javaClass,
                    "initializeController was called twice, ignoring secondary call"
                )
                return
            }
        }

        // create controllers
        LogHelper.info(this.javaClass, "initializing FensterV2 controllers")
        hostActivity = host

        config = SwipeControlsConfigurationProvider(host)
        gesture = SwipeGestureController(host, this)
        thief = host.injectTouchThief(gesture!!)
        audio = createAudioController()
        screen = createScreenController()

        // create overlay
        SwipeControlsOverlayLayout(host).let {
            overlay = it
            thief?.addView(it)
        }
    }

    /**
     * dispatch a touch event to downstream views
     *
     * @param event the event to dispatch
     * @return was the event consumed?
     */
    fun dispatchDownstreamTouchEvent(event: MotionEvent) =
        thief?.dispatchTouchEventToChildren(event)

    /**
     * create the audio volume controller
     */
    private fun createAudioController() =
        if (config?.shouldEnableFensterVolumeControl == true)
            AudioVolumeController(hostActivity!!) else null

    /**
     * create the screen brightness controller instance
     */
    private fun createScreenController() =
        if (config?.shouldEnableFensterBrightnessControl == true)
            ScreenBrightnessController(hostActivity!!) else null

    /**
     * the current screen rectangle
     */
    private val screenRect: Rectangle
        get() = Rectangle(thief!!.x.toInt(), thief!!.y.toInt(), thief!!.width, thief!!.height)

    /**
     * the rectangle of the volume control zone
     */
    val volumeZone: Rectangle
        get() = SwipeZonesHelper.getVolumeControlZone(hostActivity!!, screenRect)

    /**
     * the rectangle of the screen brightness control zone
     */
    val brightnessZone: Rectangle
        get() = SwipeZonesHelper.getBrightnessControlZone(hostActivity!!, screenRect)
}