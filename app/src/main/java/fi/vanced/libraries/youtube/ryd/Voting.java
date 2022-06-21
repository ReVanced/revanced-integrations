package fi.vanced.libraries.youtube.ryd;

import static app.revanced.integrations.settings.Settings.debug;

import android.content.Context;


import fi.vanced.libraries.youtube.ryd.requests.RYDRequester;

public class Voting {
    private static final String TAG = "VI - RYD - Voting";

    private Registration registration;
    private Context context;

    public Voting(Context context, Registration registration) {
        this.context = context;
        this.registration = registration;
    }

    public boolean sendVote(String videoId, int vote) {
        String userId = registration.getUserId();
        if (debug) {
            LogH(TAG, "Trying to vote the following video: " + videoId + " with vote " + vote + " and userId: " + userId);
        }
        return RYDRequester.sendVote(videoId, userId, vote);
    }
}
