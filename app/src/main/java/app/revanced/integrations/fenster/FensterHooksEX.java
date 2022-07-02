package app.revanced.integrations.fenster;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Hook receiver class for 'FensterV2' video swipe controls
 */
@SuppressWarnings("unused")
public final class FensterHooksEX {
    /**
     * Hook into com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity creation lifecycle
     *
     * @param target WatchWhileActivity@this reference
     * @smali Lapp/revanced/integrations/fenster/FensterHooksEX;->WatchWhileActivity_onCreateHookEX(Ljava/lang/Object;)V
     */
    public static void WatchWhileActivity_onCreateHookEX(@Nullable Object target) {
        Log.e("FensterHooksEX", "WatchWhileActivity_onCreateHookEX called");

        if (target == null) {
            Log.e("FensterHooksEX", "target is null");
            return;
        }

        if (target instanceof Activity) {
            Toast.makeText(((Activity) target).getBaseContext(), "WatchWhileActivity_onCreateHookEX", Toast.LENGTH_SHORT).show();
            Log.e("FensterHooksEX", "target is a activity!");
        }
    }
}
