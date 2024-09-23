package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.youtube.sponsorblock.ui.CreateSegmentButtonController;
import app.revanced.integrations.youtube.sponsorblock.ui.VotingButtonController;

public abstract class PlayerControlButton {
    private static final Animation fadeIn;
    private static final Animation fadeOut;
    private static final Animation fadeOutImmediate;

    private final WeakReference<ImageView> buttonRef;
    protected final BooleanSetting setting;
    protected boolean isVisible;

    static {
        // TODO: check if these durations are correct.
        fadeIn = Utils.getResourceAnimation("fade_in");
        fadeIn.setDuration(Utils.getResourceInteger("fade_duration_fast"));

        fadeOut = Utils.getResourceAnimation("fade_out");
        fadeOut.setDuration(Utils.getResourceInteger("fade_duration_scheduled"));

        fadeOutImmediate = Utils.getResourceAnimation("abc_fade_out");
        fadeOutImmediate.setDuration(Utils.getResourceInteger("fade_duration_fast"));
    }

    @NonNull
    public static Animation getButtonFadeIn() {
        return fadeIn;
    }

    @NonNull
    public static Animation getButtonFadeOut() {
        return fadeOut;
    }

    @NonNull
    public static Animation getButtonFadeOutImmediately() {
        return fadeOutImmediate;
    }

    public PlayerControlButton(@NonNull ViewGroup bottomControlsViewGroup, @NonNull String imageViewButtonId,
                               @NonNull BooleanSetting booleanSetting, @NonNull View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        Logger.printDebug(() -> "Initializing button: " + imageViewButtonId);

        ImageView imageView = Objects.requireNonNull(bottomControlsViewGroup.findViewById(
                Utils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setVisibility(View.GONE);

        imageView.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            imageView.setOnLongClickListener(longClickListener);
        }

        setting = booleanSetting;
        buttonRef = new WeakReference<>(imageView);
    }

    public void setVisibility(boolean visible, boolean immediate) {
        try {
            if (isVisible == visible) return;
            isVisible = visible;

            ImageView iView = buttonRef.get();
            if (iView == null) {
                return;
            }

            if (visible && setting.get()) {
                iView.clearAnimation();
                if (!immediate) {
                    iView.startAnimation(PlayerControlButton.getButtonFadeIn());
                }
                iView.setVisibility(View.VISIBLE);
            } else if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (!immediate) {
                    iView.startAnimation(PlayerControlButton.getButtonFadeOut());
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVisibility failure", ex);
        }
    }
}
