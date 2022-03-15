package pl.jakubweg;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.lang.ref.WeakReference;

import static fi.razerman.youtube.XGlobals.debug;
import static pl.jakubweg.PlayerController.getCurrentVideoLength;
import static pl.jakubweg.PlayerController.getLastKnownVideoTime;

public class VotingButton {
    static String TAG = "VOTING";
    static RelativeLayout _youtubeControlsLayout;
    static WeakReference<ImageView> _votingButton = new WeakReference<>(null);
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    static boolean isShowing;

    public static void initialize(Object viewStub) {
        try {
            if(debug){
                Log.d(TAG, "initializing voting button");
            }

            _youtubeControlsLayout = (RelativeLayout) viewStub;

            ImageView imageView = (ImageView)_youtubeControlsLayout
                    .findViewById(getIdentifier("voting_button", "id"));

            if (debug && imageView == null){
                Log.d(TAG, "Couldn't find imageView with tag \"voting_button\"");
            }
            if (imageView == null) return;
            imageView.setOnClickListener(SponsorBlockUtils.voteButtonListener);
            _votingButton = new WeakReference<>(imageView);

            // Animations
            fadeDurationFast = getInteger("fade_duration_fast");
            fadeDurationScheduled = getInteger("fade_duration_scheduled");
            fadeIn = getAnimation("fade_in");
            fadeIn.setDuration(fadeDurationFast);
            fadeOut = getAnimation("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);
            isShowing = true;
            changeVisibilityImmediate(false);
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    public static void changeVisibilityNegatedImmediate(boolean visible) {
        changeVisibility(!visible, true);
    }

    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        if (isShowing == visible) return;
        isShowing = visible;

        ImageView iView = _votingButton.get();
        if (_youtubeControlsLayout == null || iView == null) return;

        if (visible && shouldBeShown()) {
            if (getLastKnownVideoTime() >= getCurrentVideoLength()) {
                return;
            }
            if (debug) {
                Log.d(TAG, "Fading in");
            }
            iView.setVisibility(View.VISIBLE);
            if (!immediate)
                iView.startAnimation(fadeIn);
            return;
        }

        if (iView.getVisibility() == View.VISIBLE) {
            if (debug) {
                Log.d(TAG, "Fading out");
            }
            if (!immediate)
                iView.startAnimation(fadeOut);
            iView.setVisibility(shouldBeShown() ? View.INVISIBLE : View.GONE);
        }
    }

    static boolean shouldBeShown() {
        return SponsorBlockUtils.isSettingEnabled(SponsorBlockSettings.isVotingEnabled);
    }

    //region Helpers
    private static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private static int getInteger(String name) {
        return YouTubeTikTokRoot_Application.getAppContext().getResources().getInteger(getIdentifier(name, "integer"));
    }

    private static Animation getAnimation(String name) {
        return AnimationUtils.loadAnimation(YouTubeTikTokRoot_Application.getAppContext(), getIdentifier(name, "anim"));
    }
    //endregion
}
