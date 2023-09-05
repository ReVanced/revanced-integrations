package app.revanced.tudortmund.lockscreen;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class ShowOnLockscreenPatch {

    public static Window getWindow(AppCompatActivity activity, float brightness) {
        Window window = activity.getWindow();

        if (brightness >= 0) {
            // High brightness set, therefore show on lockscreen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(true);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        } else {
            // Ignore brightness reset when the screen gets turned off
            DisplayManager displayManager = (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            boolean isScreenOn = false;

            for (Display display : displayManager.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    isScreenOn = true;
                }
            }

            if (isScreenOn) {
                // Not show on lockscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(false);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            }
        }

        return window;
    }

}
