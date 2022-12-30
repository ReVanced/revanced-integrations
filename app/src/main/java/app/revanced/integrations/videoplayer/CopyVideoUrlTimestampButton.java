package app.revanced.integrations.videoplayer;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CopyVideoUrlTimestampButton {
    static WeakReference<ImageView> _button = new WeakReference<>(null);
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isButtonEnabled;
    static boolean isShowing;

    public static void initializeButton(Object obj) {
        try {
            LogHelper.printDebug(() -> "initializing");
            _constraintLayout = (ConstraintLayout) obj;
            isButtonEnabled = shouldBeShown();
            ImageView imageView = _constraintLayout.findViewById(getIdentifier("copy_video_url_timestamp_button", "id"));
            if (imageView == null) {
                LogHelper.printDebug(() -> "Couldn't find imageView with id \"copy_video_url_timestamp_button\"");
                return;
            }

            imageView.setOnClickListener(view -> {
                CopyVideoUrlPatch.copyUrl(true);
            });

            _button = new WeakReference<>(imageView);
            fadeDurationFast = getInteger("fade_duration_fast");
            fadeDurationScheduled = getInteger("fade_duration_scheduled");

            Animation animation = getAnimation("fade_in");
            fadeIn = animation;
            animation.setDuration(fadeDurationFast);
            Animation animation2 = getAnimation("fade_out");
            fadeOut = animation2;
            animation2.setDuration(fadeDurationScheduled);
            isShowing = true;
            changeVisibility(false);

        } catch (Exception e) {
            LogHelper.printException(() -> ("Unable to set FrameLayout"), e);
        }
    }

    public static void changeVisibility(boolean z) {
        if (isShowing == z) return;

        isShowing = z;
        ImageView imageView = _button.get();

        if (_constraintLayout == null || imageView == null)
            return;

        if (z && isButtonEnabled) {
            LogHelper.printDebug(() -> "Fading in");
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(fadeIn);
        }
        else if (imageView.getVisibility() == View.VISIBLE) {
            LogHelper.printDebug(() -> "Fading out");
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }

    private static boolean shouldBeShown() {
        return SettingsEnum.COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN.getBoolean();
    }

    private static int getIdentifier(String str, String str2) {
        Context appContext = ReVancedUtils.getContext();
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }

    private static int getInteger(String str) {
        return ReVancedUtils.getContext().getResources().getInteger(getIdentifier(str, "integer"));
    }
    private static Animation getAnimation(String str) {
        return AnimationUtils.loadAnimation(ReVancedUtils.getContext(), getIdentifier(str, "anim"));
    }
}