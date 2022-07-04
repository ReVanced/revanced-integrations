package app.revanced.integrations.fenster.controllers

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import app.revanced.integrations.fenster.util.SwipeControlZone
import app.revanced.integrations.fenster.util.getSwipeControlZone
import app.revanced.integrations.utils.LogHelper
import kotlin.math.abs

/**
 * main controller class for 'FensterV2' swipe controls
 */
class FensterController {

    /**
     * are the swipe controls currently enabled?
     */
    var isEnabled: Boolean
        get() = _isEnabled
        set(value) {
            _isEnabled = value
            overlayController?.setOverlayVisible(_isEnabled)
            LogHelper.debug(this.javaClass, "FensterController.isEnabled set to $_isEnabled")
        }
    private var _isEnabled = false

    /**
     * the activity that hosts the controller
     */
    private var hostActivity: Activity? = null
    private var audioController: AudioVolumeController? = null
    private var screenController: ScreenBrightnessController? = null
    private var overlayController: FensterOverlayController? = null

    private val gestureListener = FensterGestureListener()
    private var gestureDetector: GestureDetector? = null

    /**
     * Initializes the controller.
     * this function *may* be called after [initializeOverlay], but must be called before [onTouchEvent]
     *
     * @param host the activity that hosts the controller. this must be the same activity that the view hook for [onTouchEvent] is on
     */
    fun initializeController(host: Activity) {
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

        LogHelper.debug(this.javaClass, "initializing FensterV2 controllers")
        hostActivity = host
        audioController = AudioVolumeController(host)
        screenController = ScreenBrightnessController(host)
        gestureDetector = GestureDetector(host, gestureListener)
    }

    /**
     * Initializes the user feedback overlay, adding it as a child to the provided parent.
     * this function *may* not be called, but in that case you'll have no user feedback
     *
     * @param parent parent view group that the overlay is added to
     */
    fun initializeOverlay(parent: ViewGroup) {
        LogHelper.debug(this.javaClass, "initializing FensterV2 overlay")

        // create and add overlay
        overlayController = FensterOverlayController(parent.context)
        parent.addView(overlayController!!.overlayRootView, 0)
    }

    /**
     * Process touch events from the view hook.
     * the hooked view *must* be a child of the activity used for [initializeController]
     *
     * @param event the motion event to process
     * @return was the event consumed by the controller?
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        // if disabled, we shall not consume any events
        if (!isEnabled) return false

        // send event to gesture detector
        if (event.action == MotionEvent.ACTION_UP) {
            gestureListener.onUp(event)
        }
        val consumed = gestureDetector?.onTouchEvent(event) ?: false

        // if the event was inside a control zone, we always consume the event
        return consumed || (event.getSwipeControlZone() != SwipeControlZone.NONE)
    }

    /**
     * primary gesture listener that handles the following behaviour:
     *
     * - Volume & Brightness swipe controls:
     * when swiping on the right or left side of the screen, the volume or brightness is adjusted accordingly.
     * swipe controls are only unlocked after a long- press in the corresponding screen half
     *
     * - Fling- to- mute:
     * when quickly flinging down, the volume is instantly muted
     */
    inner class FensterGestureListener : GestureDetector.SimpleOnGestureListener() {

        /**
         * to enable swipe controls, users must first long- press. this flags monitors that long- press
         */
        private var inSwipeSession = false

        /**
         * custom handler for ACTION_UP event, because GestureDetector doesn't offer that :|
         *
         * @param e the motion event
         */
        fun onUp(e: MotionEvent) {
            LogHelper.debug(this.javaClass, "onUp(${e.x}, ${e.y}, ${e.action})")
            inSwipeSession = false
        }

        override fun onLongPress(e: MotionEvent?) {
            if (e == null) return
            LogHelper.debug(this.javaClass, "onLongPress(${e.x}, ${e.y}, ${e.action})")

            // enter swipe session with feedback
            inSwipeSession = true
            overlayController?.notifyEnterSwipeSession()

            // make the GestureDetector believe there was a ACTION_UP event
            // so it will handle further events
            e.action = MotionEvent.ACTION_UP
            gestureDetector?.onTouchEvent(e)
        }

        override fun onScroll(
            eFrom: MotionEvent?,
            eTo: MotionEvent?,
            disX: Float,
            disY: Float
        ): Boolean {
            if (eFrom == null || eTo == null) return false
            LogHelper.debug(
                this.javaClass,
                "onScroll(from: [${eFrom.x}, ${eFrom.y}, ${eFrom.action}], to: [${eTo.x}, ${eTo.y}, ${eTo.action}], d: [$disX, $disY])"
            )

            // ignore if scroll is very small OR not in scroll session
            if (abs(disY) < 1 || !inSwipeSession) return false

            // do the adjustment
            val amount = if (disY > 0) 1 else -1
            when (eFrom.getSwipeControlZone()) {
                SwipeControlZone.VOLUME_CONTROL -> {
                    audioController?.apply {
                        volume += amount
                        overlayController?.showNewVolume((volume * 100.0) / maxVolume)
                    }
                }
                SwipeControlZone.BRIGHTNESS_CONTROL -> {
                    screenController?.apply {
                        screenBrightness += amount
                        overlayController?.showNewBrightness(screenBrightness)
                    }
                }
                SwipeControlZone.NONE -> {}
            }
            return true
        }

        override fun onFling(
            eFrom: MotionEvent?,
            eTo: MotionEvent?,
            velX: Float,
            velY: Float
        ): Boolean {
            if (eFrom == null || eTo == null) return false
            LogHelper.debug(
                this.javaClass,
                "onFling(from: [${eFrom.x}, ${eFrom.y}, ${eFrom.action}], to: [${eTo.x}, ${eTo.y}, ${eTo.action}], v: [$velX, $velY])"
            )

            // filter out flings that are not very vertical
            if (abs(velY) < abs(velX * 2)) return false

            // check if either of the events was in the volume zone
            if ((eFrom.getSwipeControlZone() == SwipeControlZone.VOLUME_CONTROL) || (eTo.getSwipeControlZone() == SwipeControlZone.VOLUME_CONTROL)) {
                // if the fling was very aggressive, trigger instant- mute
                if (velY > 5000) {
                    audioController?.apply {
                        volume = 0
                        overlayController?.notifyFlingToMutePerformed()
                        overlayController?.showNewVolume((volume * 100.0) / maxVolume)
                    }
                }
            }

            return true
        }
    }
}
