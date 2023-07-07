package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.litho.ComponentHost;

import java.util.Arrays;

import app.revanced.integrations.patches.components.VideoSpeedMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CustomVideoSpeedPatch {
    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     */
    public static final float MAXIMUM_PLAYBACK_SPEED = 10;

    /**
     * Available playback speeds.
     */
    public static float[] videoSpeeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f}; // YouTube default

    /**
     * Minimum value of {@link #videoSpeeds}
     */
    public static float minVideoSpeed = 0.25f; // YouTube default

    /**
     * Maxium value of {@link #videoSpeeds}
     */
    public static float maxVideoSpeed = 2.0f; // YouTube Default

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    static {
        if (SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_ENABLED.getBoolean()) {
            loadCustomSpeeds();
        }
        // Default speed must be a available playback speed, otherwise the flyout speed menu doesn't work.
        if (!arrayContains(videoSpeeds, SettingsEnum.PLAYBACK_SPEED_DEFAULT.getFloat())) {
            LogHelper.printDebug(() -> "Resetting default playback speed");
            SettingsEnum.PLAYBACK_SPEED_DEFAULT.saveValue(SettingsEnum.PLAYBACK_SPEED_DEFAULT.defaultValue);
        }
    }

    private static void resetCustomSpeeds(@NonNull String toastMessage) {
        ReVancedUtils.showToastLong(toastMessage);
        SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.saveValue(SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.defaultValue);
    }

    private static void loadCustomSpeeds() {
        try {
            String[] speedStrings = SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.getString().split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }
            videoSpeeds = new float[speedStrings.length];
            for (int i = 0, length = speedStrings.length; i < length; i++) {
                final float speed = Float.parseFloat(speedStrings[i]);
                if (speed <= 0 || arrayContains(videoSpeeds, speed)) {
                    throw new IllegalArgumentException();
                }
                if (speed >= MAXIMUM_PLAYBACK_SPEED) {
                    resetCustomSpeeds("Custom speeds must be less than " + MAXIMUM_PLAYBACK_SPEED
                            + ".  Using default values.");
                    loadCustomSpeeds();
                    return;
                }
                minVideoSpeed = Math.min(minVideoSpeed, speed);
                maxVideoSpeed = Math.max(maxVideoSpeed, speed);
                videoSpeeds[i] = speed;
            }
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "parse error", ex);
            resetCustomSpeeds("Invalid custom video speeds. Using default values.");
            loadCustomSpeeds();
        }
    }

    private static boolean arrayContains(float[] array, float value) {
        for (float arrayValue : array) {
            if (arrayValue == value) return true;
        }
        return false;
    }

    /**
     * Initialize a settings preference list with the available playback speeds.
     */
    public static void initializeListPreference(ListPreference preference) {
        if (preferenceListEntries == null) {
            preferenceListEntries = new String[videoSpeeds.length];
            preferenceListEntryValues = new String[videoSpeeds.length];
            int i = 0;
            for (float speed : videoSpeeds) {
                String speedString = String.valueOf(speed);
                preferenceListEntries[i] = speedString + "x";
                preferenceListEntryValues[i] = speedString;
                i++;
            }
        }
        preference.setEntries(preferenceListEntries);
        preference.setEntryValues(preferenceListEntryValues);
    }

    public static void onFlyoutMenuCreate(final LinearLayout linearLayout) {
        if (!SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_ENABLED.getBoolean()) return;

        // The playback rate menu is a RecyclerView with 2 children. The third child is the "Advanced" quality menu.

        if (linearLayout.getChildCount() != 2) return;

        var view = linearLayout.getChildAt(1);
        if (!(view instanceof RecyclerView)) return;
        final var recyclerView = (RecyclerView) view;

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // ComponentHost is placed on the 1st ChildView.
                        if (VideoSpeedMenuFilterPatch.isVideoSpeedMenuVisible &&
                                recyclerView.getChildCount() == 1 &&
                                recyclerView.getChildAt(0) instanceof ComponentHost
                        ) {
                            linearLayout.setVisibility(View.GONE);

                            // Close the new video speed menu and instead show the old one.
                            showOldVideoSpeedMenu();

                            // DismissView [R.id.touch_outside] is the 1st ChildView of the 3rd ParentView.
                            ((ViewGroup) linearLayout.getParent().getParent().getParent())
                                    .getChildAt(0).performClick();
                        }

                        // Remove the listener because it will be added again.
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    public static void showOldVideoSpeedMenu() {
        LogHelper.printDebug(() -> "Old video quality menu shown");

        // Rest of the implementation added by patch.
    }
}
