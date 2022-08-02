package app.revanced.integrations.swipecontrols.controller.gesture

import android.view.MotionEvent

/**
 * recipient for touch events
 */
interface TouchEventReceiver {
    /**
     * touch event callback
     *
     * @param motionEvent the motion event that was received
     * @return intercept the event? if true, child views will not receive the event
     */
    fun onTouchEvent(motionEvent: MotionEvent): Boolean
}