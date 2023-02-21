package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.MessageFormat;

import app.revanced.integrations.patches.VideoInformation;

public class SponsorSegment implements Comparable<SponsorSegment> {
    @NonNull
    public final SegmentInfo category;
    /**
     * NULL if segment is unsubmitted
     */
    @Nullable
    public final String UUID;
    public final long start;
    public final long end;
    public final boolean isLocked;
    public boolean didAutoSkipped = false;
    /**
     * If this segment has been counted as 'skipped'
     */
    public boolean recordedAsSkipped = false;

    public SponsorSegment(@NonNull SegmentInfo category, @Nullable String UUID, long start, long end, boolean isLocked) {
        this.category = category;
        this.UUID = UUID;
        this.start = start;
        this.end = end;
        this.isLocked = isLocked;
    }

    public boolean shouldAutoSkip() {
        return category.behaviour.skip && !(didAutoSkipped && category.behaviour == SegmentBehaviour.SKIP_AUTOMATICALLY_ONCE);
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment
     */
    public boolean timeIsNearStart(long videoTime, long nearThreshold) {
        return (start - nearThreshold) <= videoTime && videoTime <= (start + nearThreshold);
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment
     */
    public boolean timeIsNearEnd(long videoTime, long nearThreshold) {
        return (end - nearThreshold) <= videoTime && videoTime <= (end + nearThreshold);
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment
     * @return if the time parameter is within or close to this segment
     */
    public boolean timeIsInsideOrNear(long videoTime, long nearThreshold) {
        return (start - nearThreshold) <= videoTime && videoTime < (end + nearThreshold);
    }

    /**
     * @return if the time parameter is outside this segment
     */
    public boolean timeIsOutside(long videoTime) {
        return start < videoTime || end <= videoTime;
    }

    /**
     * @return if the segment is completely contained inside this segment
     */
    public boolean containsSegment(SponsorSegment other) {
        return start <= other.start && other.end <= end;
    }

    /**
     * @return the length of this segment, in milliseconds.  Always a positive number.
     */
    public long length() {
        return end - start;
    }

    /**
     * @return 'skip segment' UI overlay button text
     */
    @NonNull
    public String getSkipButtonText() {
        return category.getSkipButtonText(start, VideoInformation.getCurrentVideoLength());
    }

    /**
     * @return 'skipped segment' toast message
     */
    @NonNull
    public String getSkippedToastText() {
        return category.getSkippedToastText(start, VideoInformation.getCurrentVideoLength());
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }

    @NonNull
    @Override
    public String toString() {
        return MessageFormat.format("SegmentInfo'{'category=''{0}'', start={1}, end={2}, locked={3}'}'", category, start, end, isLocked);
    }
}
