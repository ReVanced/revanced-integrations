package app.revanced.integrations.videoplayer;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public abstract class BottomControlButton {
    private static Animation fadeIn;
    private static Animation fadeOut;

    private final WeakReference<ImageView> buttonRef;
    private final SettingsEnum setting;
    protected boolean isShowing;

    public BottomControlButton(@NonNull ViewGroup viewGroup, @NonNull String viewId,
                               @NonNull SettingsEnum booleanSetting, @NonNull View.OnClickListener onClickListener) {
        LogHelper.printDebug(() -> "Initializing button: " + viewId);
        if (booleanSetting.returnType != SettingsEnum.ReturnType.BOOLEAN) {
            throw new IllegalArgumentException();
        }
        setting = booleanSetting;

        ImageView imageView = Objects.requireNonNull(viewGroup.findViewById(
                ReVancedUtils.getResourceIdentifier(viewId, "id")));
        imageView.setOnClickListener(onClickListener);
        imageView.setVisibility(View.GONE);
        buttonRef = new WeakReference<>(imageView);
        isShowing = false;

        if (fadeIn == null || fadeOut == null) {
            fadeIn = ReVancedUtils.getResourceAnimation("fade_in");
            fadeOut = ReVancedUtils.getResourceAnimation("fade_out");
            fadeIn.setDuration(ReVancedUtils.getResourceInteger("fade_duration_fast"));
            fadeOut.setDuration(ReVancedUtils.getResourceInteger("fade_duration_scheduled"));
        }
    }

    public void setVisibility(boolean showing) {
        if (isShowing == showing) return;
        isShowing = showing;

        ImageView imageView = buttonRef.get();
        if (imageView == null) {
            return;
        }

        imageView.clearAnimation();
        if (showing && setting.getBoolean()) {
            imageView.startAnimation(fadeIn);
            imageView.setVisibility(View.VISIBLE);
        } else if (imageView.getVisibility() == View.VISIBLE) {
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }
}
