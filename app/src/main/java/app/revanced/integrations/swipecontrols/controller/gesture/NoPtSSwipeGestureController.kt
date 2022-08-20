package app.revanced.integrations.swipecontrols.controller.gesture

import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity

/**
 * [SwipeGestureController], but with press-to-swipe disabled because a lot of people dislike the feature.
 * If you want to change something, try to do it in [SwipeGestureController] so that both configurations can benefit from it
 */
class NoPtSSwipeGestureController(private val controller: SwipeControlsHostActivity) :
    SwipeGestureController(controller) {

    /**
     * swipe session is activated as soon as a touch is detected in [onDown]
     * but only if player controls are not visible on touch- down
     */
    override var inSwipeSession
        get() = true
        set(_) {}

    private var lastOnDownEvent: MotionEvent? = null

    override fun shouldDropMotion(motionEvent: MotionEvent): Boolean {
        // ignore gestures with more than one pointer
        // when such a gesture is detected, dispatch the first event of the gesture to downstream
        if (motionEvent.pointerCount > 1) {
            lastOnDownEvent?.let {
                controller.dispatchDownstreamTouchEvent(it)
                it.recycle()
            }
            lastOnDownEvent = null
            return true
        }

        // ignore gestures when player controls are visible
        return if(arePlayerControlsVisible) {
            true
        } else super.shouldDropMotion(motionEvent)
    }

    override fun onDown(e: MotionEvent): Boolean {
        // save event for later
        lastOnDownEvent?.recycle()
        lastOnDownEvent = MotionEvent.obtain(e)

        // must be inside swipe zone
        return inSwipeZone(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        MotionEvent.obtain(e).let {
            it.action = MotionEvent.ACTION_DOWN
            controller.dispatchDownstreamTouchEvent(it)
            it.recycle()
        }

        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        MotionEvent.obtain(e).let {
            controller.dispatchDownstreamTouchEvent(it)
            it.recycle()
        }

        return super.onDoubleTapEvent(e)
    }

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return

        // send GestureDetector a ACTION_CANCEL event so it will handle further events
        // if this is left out, swipe-to-dismiss is triggered when scrolling down
        e.action = MotionEvent.ACTION_CANCEL
        detector.onTouchEvent(e)
    }
}
