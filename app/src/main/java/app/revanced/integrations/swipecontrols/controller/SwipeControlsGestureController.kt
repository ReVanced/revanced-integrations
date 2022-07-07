package app.revanced.integrations.swipecontrols.controller

import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.misc.ScrollDistanceHelper
import app.revanced.integrations.swipecontrols.misc.applyDimension
import app.revanced.integrations.swipecontrols.views.TouchThiefLayout
import app.revanced.integrations.swipecontrols.misc.contains
import app.revanced.integrations.swipecontrols.misc.toPoint
import app.revanced.integrations.utils.LogHelper
import kotlin.math.abs

/**
 * main gesture controller for fenster swipe controls
 *
 * @param context the context to create in
 * @param fenster reference to fenster controller instance
 */
class SwipeControlsGestureController(
    context: Context,
    private val fenster: SwipeControlsController
) :
    GestureDetector.SimpleOnGestureListener(),
    TouchThiefLayout.TouchEventListener {

    /**
     * the main gesture detector that powers everything
     */
    private val detector = GestureDetector(context, this)

    /**
     * to enable swipe controls, users must first long- press. this flags monitors that long- press
     */
    //TODO add option to disable press-to-swipe in settings, default it
    // to false since a lot of people seem to dislike it :(
    private var inSwipeSession = false

    /**
     * scroller for volume adjustment
     */
    private val volumeScroller = ScrollDistanceHelper(
        10.applyDimension(
            context,
            TypedValue.COMPLEX_UNIT_DIP
        )
    ) { _, _, direction ->
        fenster.audio?.apply {
            volume += direction
            fenster.overlay?.onVolumeChanged(volume, maxVolume)
        }
    }

    /**
     * scroller for screen brightness adjustment
     */
    private val brightnessScroller = ScrollDistanceHelper(
        1.applyDimension(
            context,
            TypedValue.COMPLEX_UNIT_DIP
        )
    ) { _, _, direction ->
        fenster.screen?.apply {
            screenBrightness += direction
            fenster.overlay?.onBrightnessChanged(screenBrightness)
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (fenster.config?.shouldEnableFenster == false) {
            return false
        }
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            onUp(motionEvent)
        }

        return detector.onTouchEvent(motionEvent) or inSwipeSession
    }

    /**
     * custom handler for ACTION_UP event, because GestureDetector doesn't offer that :|
     *
     * @param e the motion event
     */
    private fun onUp(e: MotionEvent) {
        LogHelper.debug(this.javaClass, "onUp(${e.x}, ${e.y}, ${e.action})")
        inSwipeSession = false
        volumeScroller.reset()
        brightnessScroller.reset()
    }

    override fun onDown(e: MotionEvent?): Boolean {
        //TODO check if inside swipe zone
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return
        LogHelper.debug(this.javaClass, "onLongPress(${e.x}, ${e.y}, ${e.action})")

        // enter swipe session with feedback
        inSwipeSession = true
        fenster.overlay?.onEnterSwipeSession()

        // send GestureDetector a ACTION_CANCEL event so it will handle further events
        e.action = MotionEvent.ACTION_CANCEL
        detector.onTouchEvent(e)

        // also send the event downstream to cancel any motion tracking that
        // downstream views might have started
        //TODO only cancel downstream events once a vertical scroll has actually started
        fenster.dispatchDownstreamTouchEvent(e)
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

        // ignore if scroll not in scroll session
        if (!inSwipeSession) return false

        // do the adjustment
        when (eFrom.toPoint()) {
            in fenster.volumeZone -> volumeScroller.add(disY.toDouble())
            in fenster.brightnessZone -> brightnessScroller.add(disY.toDouble())
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
        if (eFrom.toPoint() in fenster.volumeZone || eTo.toPoint() in fenster.volumeZone) {
            // if the fling was very aggressive, trigger instant- mute
            if (velY > 5000) {
                fenster.audio?.apply {
                    volume = 0
                    fenster.overlay?.onFlingToMutePerformed()
                    fenster.overlay?.onVolumeChanged(volume, maxVolume)
                }
            }
        }

        return true
    }
}