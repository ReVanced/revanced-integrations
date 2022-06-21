package fi.vanced.libraries.youtube.ryd;

import android.content.Context;


import app.revanced.integrations.log.LogHelper;
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
        LogHelper.debug(TAG, "Trying to vote the following video: " + videoId + " with vote " + vote + " and userId: " + userId);
        return RYDRequester.sendVote(videoId, userId, vote);
    }
}
