package app.revanced.integrations.youtube.settings.preference;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

/**
 * Allows using basic html for the summary text.
 */
@SuppressWarnings({"unused", "deprecation"})
@RequiresApi(api = Build.VERSION_CODES.O)
public class HtmlPreference extends Preference {
    {
        setSummary(Html.fromHtml(getSummary().toString(), FROM_HTML_MODE_COMPACT));
    }

    public HtmlPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public HtmlPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public HtmlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public HtmlPreference(Context context) {
        super(context);
    }
}
