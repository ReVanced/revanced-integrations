package app.revanced.integrations.swipecontrols.controller.gesture

import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity
import app.revanced.integrations.utils.LogHelper

/**
 * [SwipeGestureController], but with press-to-swipe disabled because a lot of people dislike the feature.
 * If you want to change something, try to do it in [SwipeGestureController] so that both configurations can benefit from it
 */
class NoPtSSwipeGestureController(controller: SwipeControlsHostActivity) :
    SwipeGestureController(controller) {

    /**
     * to disable press-to-swipe, we have to become press-to-swipe
     */
    override var inSwipeSession
        get() = true
        set(_) {}
        
    override fun onUp(e: MotionEvent) {
        LogHelper.debug(this.javaClass, "onUp(${e.x}, ${e.y}, ${e.action})")
        inSwipeSession = true
        currentSwipe = SwipeDirection.NONE
        didCancelDownstream = false
        volumeScroller.reset()
        brightnessScroller.reset()
    }
        
    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return
        LogHelper.debug(this.javaClass, "onLongPress(${e.x}, ${e.y}, ${e.action})")

        // exit swipe session with feedback
        inSwipeSession = false

    }
}
