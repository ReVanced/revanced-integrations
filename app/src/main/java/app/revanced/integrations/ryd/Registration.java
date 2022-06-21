package app.revanced.integrations.ryd;

import static app.revanced.integrations.ryd.RYDSettings.PREFERENCES_KEY_USERID;
import static app.revanced.integrations.utils.VancedUtils.getPreferences;
import static app.revanced.integrations.utils.VancedUtils.randomString;

import android.content.Context;
import android.content.SharedPreferences;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.ryd.requests.RYDRequester;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.utils.SharedPrefNames;

public class Registration {
    private static final String TAG = "VI - RYD - Registration";

    private String userId;
    private Context context;

    public Registration(Context context) {
        this.context = context;
    }

    public String getUserId() {
        return userId != null ? userId : fetchUserId();
    }

    private String fetchUserId() {
        try {
            if (this.context == null)
                throw new Exception("Unable to fetch userId because context was null");

            this.userId = SharedPrefHelper.getString(context, SharedPrefNames.RYD, PREFERENCES_KEY_USERID, null);

            if (this.userId == null) {
                this.userId = register();
            }
        } catch (Exception ex) {
            LogHelper.printException(TAG, "Unable to fetch the userId from shared preferences", ex);
        }

        return this.userId;
    }

    public void saveUserId(String userId) {
        try {
            if (this.context == null)
                throw new Exception("Unable to save userId because context was null");
            SharedPrefHelper.saveString(context, SharedPrefNames.RYD, PREFERENCES_KEY_USERID, userId);
        } catch (Exception ex) {
            LogHelper.printException(TAG, "Unable to save the userId in shared preferences", ex);
        }
    }

    private String register() {
        String userId = randomString(36);
        LogHelper.debug(TAG, "Trying to register the following userId: " + userId);
        return RYDRequester.register(userId, this);
    }
}
