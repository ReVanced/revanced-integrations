package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.MessageFormat;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public final long start;
    public final long end;
    @NonNull
    public final SegmentInfo category;
    /**
     * Can be NULL if segment is unsubmitted.
     */
    @Nullable
    public final String UUID;
    public final boolean isLocked;
    public boolean didAutoSkipped = false;

    public SponsorSegment(long start, long end, @NonNull SegmentInfo category, @Nullable String UUID, boolean isLocked) {
        this.start = start;
        this.end = end;
        this.category = category;
        this.UUID = UUID;
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

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }

    @NonNull
    @Override
    public String toString() {
        return MessageFormat.format("SegmentInfo'{'start={0}, end={1}, category=''{2}'', locked={3}'}'", start, end, category, isLocked);
    }
}
