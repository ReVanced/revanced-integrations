package app.revanced.integrations.youtube.patches;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ShortsAutoplayPatch {

    private enum ShortsLoopBehavior {
        UNKNOWN,
        /**
         * Repeat the same Short forever!
         */
        REPEAT,
        /**
         * Play once, then advanced to the next Short.
         */
        SINGLE_PLAY,
        /**
         * Pause playback after 1 play.
         */
        END_SCREEN;

        /**
         * YouTube enum value of the obfuscated enum type.
         */
        private Enum<?> ytEnumValue;
    }

    private static WeakReference<Activity> mainActivityRef = new WeakReference<>(null);


    public static void setMainActivity(Activity activity) {
        mainActivityRef = new WeakReference<>(activity);
    }

    /**
     * @return If the app is currently in background PiP mode.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static boolean isAppInBackgroundPiPMode() {
        return mainActivityRef.get().isInPictureInPictureMode();
    }

    /**
     * Injection point.
     */
    public static void setYTShortsRepeatEnum(Enum<?> ytEnum) {
        try {
            for (Enum<?> ytBehavior : Objects.requireNonNull(ytEnum.getClass().getEnumConstants())) {

                boolean foundBehavior = false;
                for (ShortsLoopBehavior rvBehavior : ShortsLoopBehavior.values()) {
                    if (ytBehavior.name().endsWith(rvBehavior.name())) {
                        rvBehavior.ytEnumValue = ytBehavior;

                        Logger.printDebug(() -> rvBehavior + " set to YT enum: " + ytBehavior.name());
                        foundBehavior = true;
                    }
                }

                if (!foundBehavior) {
                    Logger.printException(() -> "Unknown Shorts loop behavior: " + ytBehavior.name());
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setYTShortsRepeatEnum failure", ex);
        }
    }

    /**
     * Injection point.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Enum<?> changeShortsRepeatBehavior(Enum<?> original) {
        try {
            final boolean autoplay;

            if (isAppInBackgroundPiPMode()) {
                if (!VersionCheckPatch.IS_19_34_OR_GREATER) {
                    // 19.34+ is required to set background play behavior.
                    Logger.printDebug(() -> "PiP Shorts not supported, using original repeat behavior");

                    return original;
                }

                autoplay = Settings.SHORTS_AUTOPLAY_BACKGROUND.get();
            } else {
                autoplay = Settings.SHORTS_AUTOPLAY.get();
            }

            final ShortsLoopBehavior behavior = autoplay
                    ? ShortsLoopBehavior.SINGLE_PLAY
                    : ShortsLoopBehavior.REPEAT;

            if (behavior.ytEnumValue != null) {
                Logger.printDebug(() -> behavior.ytEnumValue == original
                        ? "Changing Shorts repeat behavior from: " + original.name() + " to: " + behavior.ytEnumValue
                        : "Behavior setting is same as original. Using original: " + original.name()
                );

                return behavior.ytEnumValue;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeShortsRepeatState failure", ex);
        }

        return original;
    }
}
