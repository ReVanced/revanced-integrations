package app.revanced.integrations.swipecontrols.views

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import app.revanced.integrations.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.integrations.swipecontrols.misc.SwipeControlsOverlay
import app.revanced.integrations.swipecontrols.misc.applyDimension
import kotlin.math.round

/**
 * main overlay layout for volume and brightness swipe controls
 *
 * @param context context to create in
 */
class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider
) : RelativeLayout(context), SwipeControlsOverlay {
    /**
     * DO NOT use this, for tools only
     */
    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider(context))

    private val feedbackTextView: TextView

    init {
        // init views
        val feedbackTextViewPadding = 2.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        feedbackTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(CENTER_IN_PARENT, TRUE)
                setPadding(
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding
                )
            }
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            visibility = View.GONE
        }
        addView(feedbackTextView)
    }

    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackTextView.visibility = View.GONE
    }

    /**
     * show the feedback view for a given time
     */
    private fun showFeedbackView() {
        feedbackTextView.visibility = View.VISIBLE
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        feedbackTextView.text = "Volume $newVolume"
        showFeedbackView()
    }

    override fun onBrightnessChanged(brightness: Double) {
        feedbackTextView.text =
            if (brightness > 0) "Brightness ${round(brightness).toInt()}%" else "Brightness AUTO"
        showFeedbackView()
    }

    override fun onEnterSwipeSession() {
        if (config.shouldEnableHapticFeedback) {
            performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }
}