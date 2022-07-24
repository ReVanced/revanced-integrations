package app.revanced.integrations.patches;

import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class OldQualityLayoutPatch {
    public static void showOldQualityMenu(ListView listView)
    {
        listView.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override public void run() {
                        listView.performItemClick(null, 4, 0);
                }
        }, 1);
    }
}
