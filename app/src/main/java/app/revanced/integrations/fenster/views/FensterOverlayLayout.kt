package app.revanced.integrations.fenster.views

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import app.revanced.integrations.fenster.misc.FensterOverlay
import app.revanced.integrations.fenster.misc.applyDimension
import kotlin.math.round

/**
 * main overlay layout for fenster
 *
 * @param context context to create in
 */
class FensterOverlayLayout(
    context: Context
) : RelativeLayout(context), FensterOverlay {

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
        feedbackHideHandler.postDelayed(feedbackHideCallback, 500)
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        feedbackTextView.text = "Volume $newVolume"
        showFeedbackView()
    }

    override fun onBrightnessChanged(brightness: Double) {
        feedbackTextView.text = "Brightness ${round(brightness).toInt()}%"
        showFeedbackView()
    }

    override fun onEnterSwipeSession() {
        performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    override fun onFlingToMutePerformed() {
        performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}