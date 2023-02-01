package app.revanced.integrations.sponsorblock.objects;

public class UserStats {
    public final String userName;
    public final double minutesSaved;
    public final int segmentCount;
    public final int viewCount;

    public UserStats(String userName, double minutesSaved, int segmentCount, int viewCount) {
        this.userName = userName;
        this.minutesSaved = minutesSaved;
        this.segmentCount = segmentCount;
        this.viewCount = viewCount;
    }
}