package app.revanced.integrations.swipecontrols.controller.gesture

import android.content.Context
import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.controller.SwipeControlsController

/**
 * [SwipeGestureController], but with press-to-swipe disabled because a lot of people dislike the feature.
 * If you want to change something, try to do it in [SwipeGestureController] so that both configurations can benefit from it
 */
class NoPtSSwipeGestureController(context: Context, fenster: SwipeControlsController) :
    SwipeGestureController(context, fenster) {

    /**
     * to disable press-to-swipe, we have to become press-to-swipe
     */
    override var inSwipeSession
        get() = true
        set(_) {}

    override fun onLongPress(e: MotionEvent?) {
        // ignore long presses, they are evil
    }
}
