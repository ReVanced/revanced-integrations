package app.revanced.integrations.swipecontrols.views

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * layout that allows for intercepting (or 'stealing') touch events from child views
 *
 * @param context the context to create in
 * @param listener listener for touch events
 * @param debugTouchableZone show a overlay on all zones covered by this layout
 */
class TouchThiefLayout(
    context: Context,
    private val listener: TouchEventListener,
    debugTouchableZone: Boolean = false
) : FrameLayout(context) {
    constructor(context: Context) : this(context, object : TouchEventListener {
        override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
            return false
        }
    })

    init {
        isFocusable = false
        isClickable = false

        if (debugTouchableZone) {
            val zoneOverlay = View(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.argb(50, 0, 255, 0))
                z = 9999f
            }
            addView(zoneOverlay)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (ev != null && listener.onTouchEvent(ev)) true else {
            super.dispatchTouchEvent(ev)
        }
    }

    /**
     * dispatch a touch event to all children, bypassing the thief's listener
     *
     * @param event the event to dispatch
     * @return was the event consumed by any child?
     */
    fun dispatchTouchEventToChildren(event: MotionEvent): Boolean =
        super.dispatchTouchEvent(event)

    /**
     * callback interface for [TouchThiefLayout]
     */
    interface TouchEventListener {
        /**
         * touch event callback
         *
         * @param motionEvent the motion event that was received
         * @return intercept the event? if true, child views will not receive the event
         */
        fun onTouchEvent(motionEvent: MotionEvent): Boolean
    }
}

/**
 * attach a [TouchThiefLayout] to the activity content
 *
 * @param listener the listener to set to the touch thief
 * @return the touch thief instance
 */
fun Activity.attachTouchThief(listener: TouchThiefLayout.TouchEventListener): TouchThiefLayout {
    // get targets
    val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)!!
    var ytContent = contentView.getChildAt(0)

    // detach previously attached thief first
    if(ytContent is TouchThiefLayout) {
        contentView.removeView(ytContent)
        ytContent = ytContent.getChildAt(0)
    }

    // create thief
    val thief = TouchThiefLayout(this, listener).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // insert the thief as parent to the actual content
    contentView.removeView(ytContent)
    contentView.addView(thief)
    thief.addView(ytContent)
    return thief
}

