package app.revanced.integrations.videoplayer;

import static app.revanced.integrations.utils.ResourceUtils.anim;
import static app.revanced.integrations.utils.ResourceUtils.findView;
import static app.revanced.integrations.utils.ResourceUtils.integer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import app.revanced.integrations.patches.downloads.DownloadsPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.StringRef;

public class DownloadButton {
    static WeakReference<ImageView> _button = new WeakReference<>(null);
    @SuppressLint("StaticFieldLeak")
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isDownloadButtonEnabled;
    static boolean isShowing;

    public static void initializeDownloadButton(Object obj) {
        try {
            _constraintLayout = (ConstraintLayout) obj;
            isDownloadButtonEnabled = shouldBeShown();
            ImageView imageView = findView(DownloadButton.class, _constraintLayout, "download_button");

            imageView.setOnClickListener(view -> {
                final var context = view.getContext();
                var downloaderPackageName = SettingsEnum.DOWNLOADS_PACKAGE_NAME.getString();

                boolean packageEnabled = false;
                try {
                    assert context != null;
                    packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
                } catch (PackageManager.NameNotFoundException error) {
                    LogHelper.debug(DownloadButton.class, "Downloader could not be found: " + error);
                }

                // If the package is not installed, show the toast
                if (!packageEnabled) {
                    Toast.makeText(context, downloaderPackageName + " " + StringRef.str("downloader_not_installed_warning"), Toast.LENGTH_LONG).show();
                    return;
                }

                // Launch PowerTube intent
                try {
                    var content = String.format("https://youtu.be/%s", DownloadsPatch.getCurrentVideoId());

                    var intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.setPackage(downloaderPackageName);
                    intent.putExtra("android.intent.extra.TEXT", content);
                    context.startActivity(intent);
                } catch (Exception error) {
                    LogHelper.printException(DownloadButton.class, "Failed to launch the intent", error);
                }
            });

            _button = new WeakReference<>(imageView);

            fadeDurationFast = integer("fade_duration_fast");
            fadeDurationScheduled = integer("fade_duration_scheduled");

            fadeIn = anim("fade_in");
            fadeIn.setDuration(fadeDurationFast);

            fadeOut = anim("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);

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
        if (!SettingsEnum.DOWNLOADS_BUTTON_SHOWN.getBoolean()) {
            return false;
        }
        // TODO: set to null, as this will be set by the settings page later
        final var DEFAULT = "PLAYER";
        return SharedPrefHelper.getString(
                ReVancedUtils.context(),
                SharedPrefHelper.SharedPrefNames.YOUTUBE,
                "pref_download_button_list",
                DEFAULT
        ).equalsIgnoreCase(DEFAULT);
    }
}

