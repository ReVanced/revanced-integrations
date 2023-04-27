package app.revanced.integrations.videoplayer;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public abstract class BottomControlButton {
    private static Animation fadeIn;
    private static Animation fadeOut;

    WeakReference<ConstraintLayout> constraintLayoutRef;
    WeakReference<ImageView> buttonRef;
    SettingsEnum setting;
    boolean isShowing;

    public BottomControlButton(Object obj, String viewId, SettingsEnum booleanSetting, View.OnClickListener onClickListener) {
        try {
            LogHelper.printDebug(() -> "Initializing button with id: " + viewId);
            ConstraintLayout constraintLayout = (ConstraintLayout) obj;
            constraintLayoutRef = new WeakReference<>(constraintLayout);
            setting = booleanSetting;

            ImageView imageView = constraintLayout.findViewById(ReVancedUtils.getResourceIdentifier(viewId, "id"));
            if (imageView == null) {
                LogHelper.printException(() -> "Couldn't find ImageView with id: " + viewId);
                return;
            }
            imageView.setOnClickListener(onClickListener);
            buttonRef = new WeakReference<>(imageView);

            if (fadeIn == null || fadeOut == null) {
                fadeIn = ReVancedUtils.getResourceAnimation("fade_in");
                fadeOut = ReVancedUtils.getResourceAnimation("fade_out");
                fadeIn.setDuration(ReVancedUtils.getResourceInteger("fade_duration_fast"));
                fadeOut.setDuration(ReVancedUtils.getResourceInteger("fade_duration_scheduled"));
            }

            isShowing = true;
            setVisibility(false);
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to initialize button with id: " + viewId, e);
        }
    }

    public void setVisibility(boolean showing) {
        if (isShowing == showing) return;
        isShowing = showing;

        if (constraintLayoutRef == null || buttonRef == null) {
            return; // Button failed to initialize, and should never happen
        }
        ConstraintLayout constraintLayout = constraintLayoutRef.get();
        ImageView imageView = buttonRef.get();
        if (constraintLayout == null || imageView == null) {
            return;
        }

        if (showing && setting.getBoolean()) {
            LogHelper.printDebug(() -> "Fading in");
            imageView.clearAnimation();
            imageView.startAnimation(fadeIn);
            imageView.setVisibility(View.VISIBLE);
        } else if (imageView.getVisibility() == View.VISIBLE) {
            LogHelper.printDebug(() -> "Fading out");
            imageView.clearAnimation();
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }
}
