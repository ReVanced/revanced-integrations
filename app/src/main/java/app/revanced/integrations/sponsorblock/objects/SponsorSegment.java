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
     * @return if the video time falls within this segment
     */
    public boolean containsTime(long videoTime) {
        return start <= videoTime && videoTime < end;
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
