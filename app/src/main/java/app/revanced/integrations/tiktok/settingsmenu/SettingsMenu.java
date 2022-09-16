package app.revanced.integrations.tiktok.settingsmenu;

import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bytedance.ies.ugc.aweme.commercialize.compliance.personalization.AdPersonalizationActivity;

public class SettingsMenu {
    public static void initializeSettings(AdPersonalizationActivity base) {
        LinearLayout linearLayout = new LinearLayout(base);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setFitsSystemWindows(true);
        linearLayout.setTransitionGroup(true);
        FrameLayout fragment = new FrameLayout(base);
        fragment.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        int fragmentId = View.generateViewId();
        fragment.setId(fragmentId);
        linearLayout.addView(fragment);
        base.setContentView(linearLayout);
        PreferenceFragment preferenceFragment = new ReVancedSettingsFragment();
        base.getFragmentManager().beginTransaction().replace(fragmentId, preferenceFragment).commit();
    }
}
