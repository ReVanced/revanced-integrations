package app.revanced.integrations.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


import java.lang.ref.WeakReference;

import app.revanced.integrations.patches.downloads.DownloadsPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.StringRef;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

/* loaded from: classes6.dex */
//ToDo: Refactor
public class DownloadButton {
    static WeakReference<ImageView> _button = new WeakReference<>(null);
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isDownloadButtonEnabled;
    static boolean isShowing;

    public static void initializeDownloadButton(Object obj) {
        try {
            LogHelper.debug(DownloadButton.class, "initializing");
            _constraintLayout = (ConstraintLayout) obj;
            isDownloadButtonEnabled = shouldBeShown();
            ImageView imageView = _constraintLayout.findViewById(getIdentifier("download_button", "id"));
            if (imageView == null) {
                LogHelper.debug(DownloadButton.class, "Couldn't find imageView with id \"download_button\"");
                return;
            }

            imageView.setOnClickListener(view -> {
                LogHelper.debug(DownloadButton.class, "Download button clicked");

                final var context = view.getContext();
                final var powerTubePackageName = "ussr.razar.youtube_dl";

                boolean packageEnabled = false;
                try {
                    assert context != null;
                    packageEnabled = context.getPackageManager().getApplicationInfo(powerTubePackageName, 0).enabled;
                } catch (PackageManager.NameNotFoundException error) {
                    LogHelper.debug(DownloadButton.class, "PowerTube could not be found: " + error);
                }

                // If the package is not installed, show the toast
                if (!packageEnabled) {
                    Toast.makeText(context, StringRef.str("powertube_not_installed_warning"), Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, StringRef.str("powertube_not_installed_notice"), Toast.LENGTH_LONG).show();
                    return;
                }

                // Launch PowerTube intent
                try {
                    String content = String.format("https://youtu.be/%s", DownloadsPatch.getCurrentVideoId());

                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.setPackage(powerTubePackageName);
                    intent.putExtra("android.intent.extra.TEXT", content);
                    context.startActivity(intent);

                    LogHelper.debug(DownloadButton.class, "Launched the intent with the content: " + content);
                } catch (Exception error) {
                    LogHelper.debug(DownloadButton.class, "Failed to launch the intent: " + error);
                }

                //var options = Arrays.asList("Video", "Audio").toArray(new CharSequence[0]);
                //
                //new AlertDialog.Builder(view.getContext())
                //        .setItems(options, (dialog, which) -> {
                //            LogHelper.debug(DownloadButton.class, String.valueOf(options[which]));
                //        })
                //        .show();
                // TODO: show popup and download via newpipe
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
            LogHelper.printException(DownloadButton.class, "Unable to set FrameLayout", e);
        }
    }

    public static void changeVisibility(boolean z) {
        if (isShowing == z) return;

        isShowing = z;
        ImageView imageView = _button.get();
        if (_constraintLayout != null && imageView != null) {
            if (z && isDownloadButtonEnabled) {
                LogHelper.debug(DownloadButton.class, "Fading in");
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(fadeIn);
            } else if (imageView.getVisibility() == View.VISIBLE) {
                LogHelper.debug(DownloadButton.class, "Fading out");
                imageView.startAnimation(fadeOut);
                imageView.setVisibility(View.GONE);
            }
        }
    }

    public static void refreshShouldBeShown() {
        isDownloadButtonEnabled = shouldBeShown();
    }

    private static boolean shouldBeShown() {
        if (!SettingsEnum.DOWNLOAD_BUTTON_SHOWN.getBoolean()) {
            return false;
        }

        Context appContext = ReVancedUtils.getContext();
        if (appContext == null) {
            LogHelper.printException(DownloadButton.class, "shouldBeShown - context is null!");
            return false;
        }
        String string = SharedPrefHelper.getString(appContext, SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_download_button_list", "PLAYER" /* TODO: set the default to null, as this will be set by the settings page later */);
        if (string == null || string.isEmpty()) {
            return false;
        }
        return string.equalsIgnoreCase("PLAYER");
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

