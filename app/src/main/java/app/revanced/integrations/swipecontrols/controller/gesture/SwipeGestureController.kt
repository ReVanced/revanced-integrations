package app.revanced.integrations.swipecontrols.controller.gesture

import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import app.revanced.integrations.shared.PlayerControlsVisibilityObserver
import app.revanced.integrations.shared.PlayerControlsVisibilityObserverImpl
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity
import app.revanced.integrations.swipecontrols.misc.ScrollDistanceHelper
import app.revanced.integrations.swipecontrols.misc.applyDimension
import app.revanced.integrations.swipecontrols.misc.contains
import app.revanced.integrations.swipecontrols.misc.toPoint
import app.revanced.integrations.utils.LogHelper
import kotlin.math.abs
import kotlin.math.pow

/**
 * base gesture controller for volume and brightness swipe controls controls, with press-to-swipe enabled
 * for the controller without press-to-swipe, see [NoPtSSwipeGestureController]
 *
 * @param controller reference to main controller instance
 */
@Suppress("LeakingThis")
open class SwipeGestureController(
    private val controller: SwipeControlsHostActivity
) :
    GestureDetector.SimpleOnGestureListener(),
    PlayerControlsVisibilityObserver by PlayerControlsVisibilityObserverImpl(controller) {

    /**
     * the main gesture detector that powers everything
     */
    protected open val detector = GestureDetector(controller, this)

    /**
     * to enable swipe controls, users must first long- press. this flags monitors that long- press
     * NOTE: if you dislike press-to-swipe, and want it disabled, have a look at [NoPtSSwipeGestureController]. it does exactly that
     */
    protected open var inSwipeSession = true

    /**
     * currently in- progress swipe
     */
    protected open var currentSwipe: SwipeDirection = SwipeDirection.NONE

    /**
     * were downstream event cancelled already? used by [onScroll]
     */
    protected open var didCancelDownstream = false

    /**
     * should [onTouchEvent] force- intercept all touch events?
     */
    protected open val shouldForceInterceptEvents: Boolean
        get() = currentSwipe == SwipeDirection.VERTICAL && inSwipeSession

    /**
     * scroller for volume adjustment
     */
    protected open val volumeScroller = ScrollDistanceHelper(
        10.applyDimension(
            controller,
            TypedValue.COMPLEX_UNIT_DIP
        )
    ) { _, _, direction ->
        controller.audio?.apply {
            volume += direction
            controller.overlay.onVolumeChanged(volume, maxVolume)
        }
    }

    /**
     * scroller for screen brightness adjustment
     */
    protected open val brightnessScroller = ScrollDistanceHelper(
        1.applyDimension(
            controller,
            TypedValue.COMPLEX_UNIT_DIP
        )
    ) { _, _, direction ->
        controller.screen?.apply {
            if (screenBrightness > 0 || direction > 0) {
                screenBrightness += direction
            } else {
                restoreDefaultBrightness()
            }

            controller.overlay.onBrightnessChanged(screenBrightness)
        }
    }

    /**
     * touch event callback
     *
     * @param motionEvent the motion event that was received
     * @return intercept the event? if true, child views will not receive the event
     */
    fun onTouchEvent(e: MotionEvent): Boolean {
        // ignore if swipe is disabled
        if (!controller.config.enableSwipeControls) {
            return false
        }

        // drop the motion event if requested
        val dropped = shouldDropMotion(e)
        val motionEvent = if (dropped) {
            // create a copy and overwrite action
            MotionEvent.obtain(e).apply {
                action = MotionEvent.ACTION_CANCEL
            }
        } else e

        // send the event to the detector
        val consumed = if (shouldForceInterceptEvents || inSwipeZone(motionEvent)) {
            detector.onTouchEvent(motionEvent) || shouldForceInterceptEvents
        } else false

        // invoke custom onUp handler
        if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
            onUp(motionEvent)
        }

        // recycle the copy of the motion event if we created a copy
        if (dropped) {
            motionEvent.recycle()
        }

        // do not consume dropped events
        return !dropped && consumed
    }

    /**
     * check if provided motion event is in any active swipe zone?
     *
     * @param e the event to check
     * @return is the event in any active swipe zone?
     */
    open fun inSwipeZone(e: MotionEvent): Boolean {
        val inVolumeZone = if (controller.config.enableVolumeControls)
            (e.toPoint() in controller.zones.volume) else false
        val inBrightnessZone = if (controller.config.enableBrightnessControl)
            (e.toPoint() in controller.zones.brightness) else false

        return inVolumeZone || inBrightnessZone
    }

    //TODO docu
    open fun shouldDropMotion(motionEvent: MotionEvent): Boolean = false

    /**
     * custom handler for ACTION_UP event, because GestureDetector doesn't offer that :|
     * basically just resets all flags to non- swiping values
     *
     * @param e the motion event
     */
    open fun onUp(e: MotionEvent) {
        LogHelper.debug(this.javaClass, "onUp(${e.x}, ${e.y}, ${e.action})")
        inSwipeSession = false
        currentSwipe = SwipeDirection.NONE
        didCancelDownstream = false
        volumeScroller.reset()
        brightnessScroller.reset()
    }

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return
        LogHelper.debug(this.javaClass, "onLongPress(${e.x}, ${e.y}, ${e.action})")

//        //check that player controls are not currently visible before entering swipe session
//        if (arePlayerControlsVisible) {
//            return
//        }

        // enter swipe session with feedback
        inSwipeSession = true
        controller.overlay.onEnterSwipeSession()

        // send GestureDetector a ACTION_CANCEL event so it will handle further events
        e.action = MotionEvent.ACTION_CANCEL
        detector.onTouchEvent(e)
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

        return when (currentSwipe) {
            // no swipe direction was detected yet, try to detect one
            // if the user did not swipe far enough, we cannot detect what direction they swiped
            // so we wait until a greater distance was swiped
            // NOTE: sqrt() can be high- cost, so using squared magnitudes here
            SwipeDirection.NONE -> {
                val deltaX = abs(eTo.x - eFrom.x)
                val deltaY = abs(eTo.y - eFrom.y)
                val swipeMagnitudeSquared = deltaX.pow(2) + deltaY.pow(2)
                if (swipeMagnitudeSquared > controller.config.swipeMagnitudeThreshold.pow(2)) {
                    currentSwipe = if (deltaY > deltaX) {
                        SwipeDirection.VERTICAL
                    } else {
                        SwipeDirection.HORIZONTAL
                    }
                }

                return false
            }

            // horizontal swipe, we should leave this one for downstream to handle
            SwipeDirection.HORIZONTAL -> false

            // vertical swipe, could be for us
            SwipeDirection.VERTICAL -> {
                if (!inSwipeSession) {
                    // not in swipe session, let downstream handle this one
                    return false
                }

                // vertical & in swipe session, handle this one:
                // first, send ACTION_CANCEL to downstream to let them known they should stop tracking events
                if (!didCancelDownstream) {
                    val eCancel = MotionEvent.obtain(eFrom)
                    eCancel.action = MotionEvent.ACTION_CANCEL
                    controller.dispatchDownstreamTouchEvent(eCancel)
                    eCancel.recycle()
                    didCancelDownstream = true
                }

                // then, process the event
                when (eFrom.toPoint()) {
                    in controller.zones.volume -> volumeScroller.add(disY.toDouble())
                    in controller.zones.brightness -> brightnessScroller.add(disY.toDouble())
                }
                return true
            }
        }
    }

    /**
     * direction of a swipe
     */
    enum class SwipeDirection {
        /**
         * swipe has no direction or no swipe
         */
        NONE,

        /**
         * swipe along the X- Axes
         */
        HORIZONTAL,

        /**
         * swipe along the Y- Axes
         */
        VERTICAL
    }
}
