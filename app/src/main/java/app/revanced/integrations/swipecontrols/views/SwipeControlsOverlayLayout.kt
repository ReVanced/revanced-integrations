package app.revanced.integrations.swipecontrols.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import app.revanced.integrations.utils.ReVancedUtils
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
    private val autoBrightnessIcon: Drawable
    private val manualBrightnessIcon: Drawable
    private val mutedVolumeIcon: Drawable
    private val normalVolumeIcon: Drawable

    private fun getDrawable(name: String): Drawable {
        return resources.getDrawable(
            ReVancedUtils.getResourceIdByName(context, "drawable", name),
            context.theme
        ).apply {
            setTint(Color.WHITE)
        }
    }

    init {
        // get icon ids
        autoBrightnessIcon = getDrawable("ic_sc_brightness_auto")
        manualBrightnessIcon = getDrawable("ic_sc_brightness_manual")
        mutedVolumeIcon = getDrawable("ic_sc_volume_mute")
        normalVolumeIcon = getDrawable("ic_sc_volume_normal")

        // init views
        val feedbackTextViewPadding = 2.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val compoundIconPadding = 4.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
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
            setBackgroundColor(Color.argb(127, 0, 0, 0))
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            compoundDrawablePadding = compoundIconPadding
            visibility = GONE
        }
        addView(feedbackTextView)
    }

    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackTextView.visibility = View.GONE
    }

    /**
     * show the feedback view for a given time
     *
     * @param message the message to show
     * @param icon the icon to use
     */
    private fun showFeedbackView(message: String, icon: Drawable) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)
        feedbackTextView.apply {
            text = message
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                null,
                null,
                null
            )
            visibility = VISIBLE
        }
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        showFeedbackView(
            "$newVolume",
            if (newVolume > 0) normalVolumeIcon else mutedVolumeIcon
        )
    }

    override fun onBrightnessChanged(brightness: Double) {
        if (brightness > 0) {
            showFeedbackView("${round(brightness).toInt()}%", manualBrightnessIcon)
        } else {
            showFeedbackView("AUTO", autoBrightnessIcon)
        }
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