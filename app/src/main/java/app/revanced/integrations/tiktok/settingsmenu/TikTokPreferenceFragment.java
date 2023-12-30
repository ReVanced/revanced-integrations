package app.revanced.integrations.tiktok.settingsmenu;

import android.preference.PreferenceScreen;
import app.revanced.integrations.shared.settingsmenu.AbstractPreferenceFragment;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.DownloadsPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.FeedFilterPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.IntegrationsPreferenceCategory;
import app.revanced.integrations.tiktok.settingsmenu.preference.categories.SimSpoofPreferenceCategory;

@SuppressWarnings("deprecation")
public class TikTokPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void initialize() {
        final var context = getContext();

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new IntegrationsPreferenceCategory(context, preferenceScreen);
    }
}
