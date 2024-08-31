package app.revanced.integrations.youtube.returnyoutubedislike.requests;

import static app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ReturnYouTubeDislike API estimated like/dislike/view counts.
 *
 * ReturnYouTubeDislike does not guarantee when the counts are updated.
 * So these values may lag behind what YouTube shows.
 */
public final class RYDVoteData {
    @NonNull
    public final String videoId;

    /**
     * Estimated number of views
     */
    public final long viewCount;

    private final long fetchedLikeCount;
    private volatile long likeCount; // Read/write from different threads.

    /**
     * Like count can be hidden by video creator, but RYD still tracks the number
     * of like/dislikes it received thru it's browser extension and and API.
     * The raw like/dislikes can be used to calculate a percentage.
     *
     * Raw values can be null, especially for older videos with little to no views.
     */
    @Nullable
    private final Long fetchedRawLikeCount;
    @Nullable
    private volatile Long rawLikeCount;

    private volatile float likePercentage;

    private final long fetchedDislikeCount;
    private volatile long dislikeCount; // Read/write from different threads.

    @Nullable
    private final Long fetchedRawDislikeCount;
    @Nullable
    private volatile Long rawDislikeCount;

    private volatile float dislikePercentage;

    @Nullable
    private static Long getLongIfExist(JSONObject json, String key) throws JSONException {
        return json.isNull(key)
                ? null
                : json.getLong(key);
    }

    /**
     * @throws JSONException if JSON parse error occurs, or if the values make no sense (ie: negative values)
     */
    public RYDVoteData(@NonNull JSONObject json) throws JSONException {
        videoId = json.getString("id");
        viewCount = json.getLong("viewCount");

        fetchedLikeCount = json.getLong("likes");
        fetchedRawLikeCount = getLongIfExist(json, "rawLikes");

        fetchedDislikeCount = json.getLong("dislikes");
        fetchedRawDislikeCount = getLongIfExist(json, "rawDislikes");

        if (viewCount < 0 || fetchedLikeCount < 0 || fetchedDislikeCount < 0) {
            throw new JSONException("Unexpected JSON values: " + json);
        }
        likeCount = fetchedLikeCount;
        dislikeCount = fetchedDislikeCount;

        updateUsingVote(Vote.LIKE_REMOVE); // Calculate percentages.
    }

    /**
     * Public like count of the video, as reported by YT when RYD last updated it's data.
     * Value will always be zero if the video is hidden by the YT creator.
     */
    public long getLikeCount() {
        return likeCount;
    }

    /**
     * Estimated total dislike count, extrapolated from the public like count using RYD data.
     */
    public long getDislikeCount() {
        return dislikeCount;
    }

    /**
     * Estimated percentage of likes for all votes.  Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a likePercentage of 0.8
     */
    public float getLikePercentage() {
        return likePercentage;
    }

    /**
     * Estimated percentage of dislikes for all votes. Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a dislikePercentage of 0.2
     */
    public float getDislikePercentage() {
        return dislikePercentage;
    }

    public void updateUsingVote(Vote vote) {
        final int likesToAdd, dislikesToAdd;

        switch (vote) {
            case LIKE:
                likesToAdd = 1;
                dislikesToAdd = 0;
                break;
            case DISLIKE:
                likesToAdd = 0;
                dislikesToAdd = 1;
                break;
            case LIKE_REMOVE:
                likesToAdd = 0;
                dislikesToAdd = 0;
                break;
            default:
                throw new IllegalStateException();
        }

        // If a video has no public likes but RYD has raw like data,
        // then use the raw data instead.
        Long localRawLikeCount = fetchedRawLikeCount;
        Long localRawDislikeCount = fetchedRawDislikeCount;
        final boolean hasRawData = localRawLikeCount != null && localRawDislikeCount != null;
        final boolean videoHasNoPublicLikes = fetchedLikeCount == 0;

        likeCount = fetchedLikeCount + likesToAdd;
        // RYD now always returns an estimated dislike count, even if the likes are hidden.
        dislikeCount = fetchedDislikeCount + dislikesToAdd;

        if (hasRawData) {
            localRawLikeCount += likesToAdd;
            localRawDislikeCount += dislikesToAdd;
            rawLikeCount = localRawLikeCount;
            rawDislikeCount = localRawDislikeCount;
        } else {
            rawLikeCount = null;
            rawDislikeCount = null;
        }

        // Update percentages.

        if (videoHasNoPublicLikes && hasRawData) {
            // Video creator has hidden the like count,
            // but can use the raw like/dislikes to calculate a percentage.
            final float totalRawCount = localRawLikeCount + localRawDislikeCount;
            if (totalRawCount == 0) {
                likePercentage = 0;
                dislikePercentage = 0;
            } else {
                likePercentage = localRawLikeCount / totalRawCount;
                dislikePercentage = localRawDislikeCount / totalRawCount;
            }
        } else {
            final float totalCount = likeCount + dislikeCount;
            if (totalCount == 0) {
                likePercentage = 0;
                dislikePercentage = 0;
            } else {
                likePercentage = likeCount / totalCount;
                dislikePercentage = dislikeCount / totalCount;
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "RYDVoteData{"
                + "videoId=" + videoId
                + ", viewCount=" + viewCount
                + ", likeCount=" + likeCount
                + ", rawLikeCount=" + rawLikeCount
                + ", dislikeCount=" + dislikeCount
                + ", rawDislikeCount=" + rawDislikeCount
                + ", likePercentage=" + likePercentage
                + ", dislikePercentage=" + dislikePercentage
                + '}';
    }

    // equals and hashcode is not implemented (currently not needed)

}
