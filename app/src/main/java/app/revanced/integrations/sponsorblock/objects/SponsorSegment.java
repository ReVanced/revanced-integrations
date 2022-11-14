package app.revanced.integrations.sponsorblock.objects;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public final long start;
    public final long end;
    public final SponsorBlockSettings.SegmentInfo category;
    public final String uuid;
    public final boolean isLocked;
    public boolean hasAutoSkipped = false;

    public SponsorSegment(long start, long end, SponsorBlockSettings.SegmentInfo category, String uuid, boolean isLocked) {
        this.start = start;
        this.end = end;
        this.category = category;
        this.uuid = uuid;
        this.isLocked = isLocked;
    }

    @NonNull
    @Override
    public String toString() {
        return "SponsorSegment{" +
                "start=" + start +
                ", end=" + end +
                ", category=" + category +
                ", uuid='" + uuid + '\'' +
                ", isLocked=" + isLocked +
                ", hasAutoSkipped=" + hasAutoSkipped +
                '}';
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }
}
