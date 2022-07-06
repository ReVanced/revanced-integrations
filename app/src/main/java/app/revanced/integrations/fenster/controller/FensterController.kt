package app.revanced.integrations.fenster.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import app.revanced.integrations.fenster.FensterConfigurationProvider
import app.revanced.integrations.fenster.misc.Rectangle
import app.revanced.integrations.fenster.misc.SwipeZonesHelper
import app.revanced.integrations.fenster.views.FensterOverlayLayout
import app.revanced.integrations.fenster.views.TouchThiefLayout
import app.revanced.integrations.fenster.views.injectTouchThief
import app.revanced.integrations.utils.LogHelper

/**
 * The main controller for 'FensterV2' Swipe controls
 */
//TODO options to remove this supression?
@SuppressLint("StaticFieldLeak")
object FensterController {

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
     * current instance of [FensterConfigurationProvider]
     */
    var config: FensterConfigurationProvider? = null
        private set

    /**
     * current instance of [FensterOverlayLayout]
     */
    var overlay: FensterOverlayLayout? = null
        private set

    private var hostActivity: Activity? = null
    private var gesture: FensterGestureController? = null
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

        config = FensterConfigurationProvider(host)
        gesture = FensterGestureController(host, this)
        thief = host.injectTouchThief(gesture!!)
        audio = createAudioController()
        screen = createScreenController()

        // create overlay
        overlay = FensterOverlayLayout(host)
        thief?.addView(overlay)
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
     * create the screen brithness controller instance
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