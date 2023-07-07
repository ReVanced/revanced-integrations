package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import app.revanced.integrations.patches.components.VideoSpeedMenuFilterPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import com.facebook.litho.ComponentHost;

import java.util.Arrays;

public class CustomVideoSpeedPatch {
    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     */
    public static final float MAXIMUM_PLAYBACK_SPEED = 10;

    /**
     * Custom playback speeds.
     */
    public static float[] customVideoSpeeds;

    /**
     * Minimum value of {@link #customVideoSpeeds}
     */
    public static float minVideoSpeed;

    /**
     * Maxium value of {@link #customVideoSpeeds}
     */
    public static float maxVideoSpeed;

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    static {
        loadSpeeds();
    }

    private static void resetCustomSpeeds(@NonNull String toastMessage) {
        ReVancedUtils.showToastLong(toastMessage);
        SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.saveValue(SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.defaultValue);
    }

    private static void loadSpeeds() {
        try {
            String[] speedStrings = SettingsEnum.CUSTOM_PLAYBACK_SPEEDS_LIST.getString().split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }
            customVideoSpeeds = new float[speedStrings.length];
            for (int i = 0, length = speedStrings.length; i < length; i++) {
                final float speed = Float.parseFloat(speedStrings[i]);
                if (speed <= 0 || arrayContains(customVideoSpeeds, speed)) {
                    throw new IllegalArgumentException();
                }
                if (speed >= MAXIMUM_PLAYBACK_SPEED) {
                    resetCustomSpeeds("Custom speeds must be less than " + MAXIMUM_PLAYBACK_SPEED
                            + ".  Using default values.");
                    loadSpeeds();
                    return;
                }
                minVideoSpeed = Math.min(minVideoSpeed, speed);
                maxVideoSpeed = Math.max(maxVideoSpeed, speed);
                customVideoSpeeds[i] = speed;
            }
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "parse error", ex);
            resetCustomSpeeds("Invalid custom video speeds. Using default values.");
            loadSpeeds();
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
            preferenceListEntries = new String[customVideoSpeeds.length];
            preferenceListEntryValues = new String[customVideoSpeeds.length];
            int i = 0;
            for (float speed : customVideoSpeeds) {
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
