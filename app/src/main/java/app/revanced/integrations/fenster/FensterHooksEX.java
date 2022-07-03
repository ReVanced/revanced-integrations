package app.revanced.integrations.fenster;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * Hook receiver class for 'FensterV2' video swipe controls
 */
@SuppressWarnings("unused")
public final class FensterHooksEX {


    public static void WatchWhileActivity_onStartHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof Activity) {
            Log.i("FensterHooksEX", "WatchWhileActivity_onStartHookEX");
            //Toast.makeText(((Activity) thisRef).getBaseContext(), "WatchWhileActivity_onStartHookEX", Toast.LENGTH_SHORT).show();
        }
    }

    public static void YouTubePlayerOverlaysLayout_onFinishInflateHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof ViewGroup) {
            Log.e("FensterHooksEX", "YouTubePlayerOverlaysLayout_onFinishInflateHookEX");
            //Toast.makeText(((ViewGroup) thisRef).getContext(), "YouTubePlayerOverlaysLayout_onFinishInflateHookEX", Toast.LENGTH_SHORT).show();

            // test adding a player overlay
            final ViewGroup vg = ((ViewGroup) thisRef);
            TextView text = new TextView(vg.getContext());
            text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            text.setClickable(false);
            text.setFocusable(false);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(Color.RED);
            text.setText("Dummy Overlay");
            vg.addView(text, 0);
        }
    }

    public static void YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(@Nullable Object type) {
        //if(type == null) return;
        Log.i("FensterHooksEx", "PlayerType updated to " + type);
    }

    public static boolean NextGenWatchLayout_onTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        if (motionEvent == null) return false;
        if (motionEvent instanceof MotionEvent) {
            MotionEvent e = (MotionEvent) motionEvent;
            Log.i("FensterHooksEX", "NextGenWatchLayout_onTouchEventHookEX(" + e.getX() + ", " + e.getY() + ", " + e.getAction() + ")");

            // consume the event
            //return true;
        }

        return false;
    }

    public static boolean NextGenWatchLayout_onInterceptTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        if (motionEvent == null) return false;
        if (motionEvent instanceof MotionEvent) {
            MotionEvent e = (MotionEvent) motionEvent;
            Log.i("FensterHooksEX", "NextGenWatchLayout_onInterceptTouchEventHookEX(" + e.getX() + ", " + e.getY() + ", " + e.getAction() + ")");

            // consume the event
            //return true;
        }

        return false;
    }
}
