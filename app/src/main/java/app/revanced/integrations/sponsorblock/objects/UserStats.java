package app.revanced.integrations.sponsorblock.objects;

import androidx.annotation.NonNull;

import java.util.Objects;

public class UserStats {
    @NonNull
    public final String userId;
    @NonNull
    public final String userName;
    public final double minutesSaved;
    public final int segmentCount;
    public final int viewCount;

    public UserStats(@NonNull String userId, @NonNull String userName, double minutesSaved, int segmentCount, int viewCount) {
        this.userId = Objects.requireNonNull(userId);
        this.userName = Objects.requireNonNull(userName);
        this.minutesSaved = minutesSaved;
        this.segmentCount = segmentCount;
        this.viewCount = viewCount;
    }
}