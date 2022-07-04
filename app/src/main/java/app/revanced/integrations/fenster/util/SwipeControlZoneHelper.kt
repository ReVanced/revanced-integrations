package app.revanced.integrations.fenster.util

import android.view.MotionEvent

/**
 * zones for swipe controls
 */
enum class SwipeControlZone {
    /**
     * not in any zone, should do nothing
     */
    NONE,

    /**
     * in volume zone, adjust volume
     */
    VOLUME_CONTROL,

    /**
     * in brightness zone, adjust brightness
     */
    BRIGHTNESS_CONTROL;
}

/**
 * get the control zone in which this motion event is
 *
 * @return the swipe control zone
 */
fun MotionEvent.getSwipeControlZone(): SwipeControlZone {
    // get screen size
    val screenWidth = device.getMotionRange(MotionEvent.AXIS_X).range
    val screenHeight = device.getMotionRange(MotionEvent.AXIS_Y).range

    // check in what part of the detection zone the event is
    //  1       off           ⅓       ⅔           w-off     1
    //  |  none  | brightness |  none  |   volume   |  none  |
    val offset = 50f
    val width = screenWidth - (offset * 2)
    val height = screenHeight - (offset * 2)
    val oneThird = offset + (width / 3)
    val twoThird = offset + (width * 2 / 3)
    if (y in offset..height) {
        return when (x) {
            in offset..oneThird -> SwipeControlZone.BRIGHTNESS_CONTROL
            in twoThird..width -> SwipeControlZone.VOLUME_CONTROL
            else -> SwipeControlZone.NONE
        }
    }

    // not in bounds
    return SwipeControlZone.NONE
}
