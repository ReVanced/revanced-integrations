package app.revanced.integrations.swipecontrols.controller.gesture.core

import android.view.GestureDetector
import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity

/**
 * the common base of all [GestureController] classes.
 * handles most of the boilerplate code needed for gesture detection
 *
 * @param controller reference to the main swipe controller
 */
abstract class BaseGestureController(
    private val controller: SwipeControlsHostActivity
) : GestureController,
    GestureDetector.SimpleOnGestureListener(),
    SwipeDetector by SwipeDetectorImpl(
        controller.config.swipeMagnitudeThreshold.toDouble()
    ),
    VolumeAndBrightnessScroller by VolumeAndBrightnessScrollerImpl(
        controller,
        controller.audio,
        controller.screen,
        controller.overlay,
        10,
        1
    ) {

    /**
     * the main gesture detector that powers everything
     */
    @Suppress("LeakingThis")
    protected val detector = GestureDetector(controller, this)

    /**
     * were downstream event cancelled already? used in [onScroll]
     */
    private var didCancelDownstream = false

    override fun submitTouchEvent(motionEvent: MotionEvent): Boolean {
        // ignore if swipe is disabled
        if (!controller.config.enableSwipeControls) {
            return false
        }

        // drop the motion event if requested
        val dropped = shouldDropMotion(motionEvent)
        val event = if (dropped) {
            // create a copy and overwrite action
            MotionEvent.obtain(motionEvent).apply {
                action = MotionEvent.ACTION_CANCEL
            }
        } else motionEvent

        // send the event to the detector
        val consumed = if (shouldForceInterceptEvents || isInSwipeZone(event)) {
            detector.onTouchEvent(event) || shouldForceInterceptEvents
        } else false

        // invoke custom onUp handler
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            onUp(event)
        }

        // recycle the copy of the motion event if we created a copy
        if (dropped) {
            event.recycle()
        }

        // do not consume dropped events
        return !dropped && consumed
    }

    /**
     * custom handler for [MotionEvent.ACTION_UP] event, because GestureDetector doesn't offer that :|
     *
     * @param e the motion event
     */
    open fun onUp(e: MotionEvent) {
        didCancelDownstream = false
        resetSwipe()
        resetScroller()
    }

    override fun onScroll(
        from: MotionEvent,
        to: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // submit to swipe detector
        submitForSwipe(from, to, distanceX, distanceY)

        // call swipe callback if in a swipe
        return if (currentSwipe != SwipeDetector.SwipeDirection.NONE) {
            val consumed = onSwipe(
                from,
                to,
                distanceX.toDouble(),
                distanceY.toDouble()
            )

            // if the swipe was consumed, cancel downstream events once
            if (consumed && !didCancelDownstream) {
                didCancelDownstream = true
                MotionEvent.obtain(from).let {
                    it.action = MotionEvent.ACTION_CANCEL
                    controller.dispatchDownstreamTouchEvent(it)
                    it.recycle()
                }
            }

            consumed
        } else false
    }

    /**
     * should [submitTouchEvent] force- intercept all touch events?
     */
    abstract val shouldForceInterceptEvents: Boolean

    /**
     * check if provided motion event is in any active swipe zone?
     *
     * @param e the event to check
     * @return is the event in any active swipe zone?
     */
    abstract fun isInSwipeZone(e: MotionEvent): Boolean

    /**
     * check if a touch event should be dropped.
     * when a event is dropped, the gesture detector received a [MotionEvent.ACTION_CANCEL] event and the event is not consumed
     *
     * @param motionEvent the event to check
     * @return should the event be dropped?
     */
    abstract fun shouldDropMotion(motionEvent: MotionEvent): Boolean

    /**
     * handler for swipe events, once a swipe is detected.
     * the direction of the swipe can be accessed in [currentSwipe]
     *
     * @param from start event of the swipe
     * @param to end event of the swipe
     * @param distanceX the horizontal distance of the swipe
     * @param distanceY the vertical distance of the swipe
     * @return was the event consumed?
     */
    abstract fun onSwipe(
        from: MotionEvent,
        to: MotionEvent,
        distanceX: Double,
        distanceY: Double
    ): Boolean
}
