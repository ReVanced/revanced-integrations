package app.revanced.integrations.youtube.patches;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ChangeShortsRepeatPatch {

    public enum ShortsLoopBehavior {
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
        Activity mainActivity = mainActivityRef.get();

        if (mainActivity == null) {
            // Should never happen, since the context is the main activity.
            Logger.printException(() -> "Activity is null, cannot determine repeat behavior");
            return false;
        }

        final boolean isPiP =  mainActivity.isInPictureInPictureMode();
        if (isPiP) {
            Logger.printDebug(() -> "Short is in background PiP mode");
        }

        return isPiP;
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
            // 19.34+ is required to set background play behavior.
            final boolean inPiPModeAndCanChangeBehavior =
                    VersionCheckPatch.IS_19_34_OR_GREATER && isAppInBackgroundPiPMode();

            ShortsLoopBehavior behavior = inPiPModeAndCanChangeBehavior
                    ? Settings.CHANGE_SHORTS_BACKGROUND_REPEAT_STATE.get()
                    : Settings.CHANGE_SHORTS_REPEAT_STATE.get();

            if (behavior == ShortsLoopBehavior.UNKNOWN) {
                Logger.printDebug(() -> "Behavior setting is default. "
                        + "Using original: " + original.name());
                return original;
            }

            if (behavior.ytEnumValue != null) {
                Logger.printDebug(() -> behavior.ytEnumValue == original
                        ? "Changing Shorts repeat behavior from: " + original.name() + " to: " + behavior.name()
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
