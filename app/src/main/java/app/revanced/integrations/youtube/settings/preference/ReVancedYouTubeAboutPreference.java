package app.revanced.integrations.youtube.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import app.revanced.integrations.shared.settings.preference.ReVancedAboutPreference;
import app.revanced.integrations.youtube.ThemeHelper;

@SuppressWarnings("unused")
public class ReVancedYouTubeAboutPreference extends ReVancedAboutPreference {

    public int getForegroundColor() {
        return ThemeHelper.getCurrentForegroundColor();
    }

    public int getBackgroundColor() {
        return ThemeHelper.getCurrentBackgroundColor();
    }

    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReVancedYouTubeAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReVancedYouTubeAboutPreference(Context context) {
        super(context);
    }
}
