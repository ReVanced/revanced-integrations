package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.StringRef.sf;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.IGNORE;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.MANUAL_SKIP;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.SKIP_AUTOMATICALLY;

import android.graphics.Paint;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import app.revanced.integrations.sponsorblock.StringRef;

public enum SegmentCategory {
    SPONSOR("sponsor", sf("sb_segments_sponsor"), sf("sb_segments_sponsor_sum"), sf("sb_skip_button_sponsor"), sf("sb_skipped_sponsor"),
            SKIP_AUTOMATICALLY, 0x00d400),
    SELF_PROMO("selfpromo", sf("sb_segments_selfpromo"), sf("sb_segments_selfpromo_sum"), sf("sb_skip_button_selfpromo"), sf("sb_skipped_selfpromo"),
            SKIP_AUTOMATICALLY, 0xffff00),
    INTERACTION("interaction", sf("sb_segments_interaction"), sf("sb_segments_interaction_sum"), sf("sb_skip_button_interaction"), sf("sb_skipped_interaction"),
            SKIP_AUTOMATICALLY, 0xcc00ff),
    INTRO("intro", sf("sb_segments_intro"), sf("sb_segments_intro_sum"),
            sf("sb_skip_button_intro_beginning"), sf("sb_skip_button_intro_middle"), sf("sb_skip_button_intro_end"),
            sf("sb_skipped_intro_beginning"), sf("sb_skipped_intro_middle"), sf("sb_skipped_intro_end"),
            MANUAL_SKIP, 0x00ffff),
    OUTRO("outro", sf("sb_segments_outro"), sf("sb_segments_outro_sum"), sf("sb_skip_button_outro"), sf("sb_skipped_outro"),
            MANUAL_SKIP, 0x0202ed),
    PREVIEW("preview", sf("sb_segments_preview"), sf("sb_segments_preview_sum"),
            sf("sb_skip_button_preview_beginning"), sf("sb_skip_button_preview_middle"), sf("sb_skip_button_preview_end"),
            sf("sb_skipped_preview_beginning"), sf("sb_skipped_preview_middle"), sf("sb_skipped_preview_end"),
            IGNORE, 0x008fd6),
    FILLER("filler", sf("sb_segments_filler"), sf("sb_segments_filler_sum"), sf("sb_skip_button_filler"), sf("sb_skipped_filler"),
            IGNORE, 0x7300FF),
    MUSIC_OFFTOPIC("music_offtopic", sf("sb_segments_nomusic"), sf("sb_segments_nomusic_sum"), sf("sb_skip_button_nomusic"), sf("sb_skipped_nomusic"),
            MANUAL_SKIP, 0xff9900),
    UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("sb_skip_button_unsubmitted"), sf("sb_skipped_unsubmitted"),
            SKIP_AUTOMATICALLY, 0xFFFFFF);

    private static final SegmentCategory[] mValuesWithoutUnsubmitted = new SegmentCategory[]{
            SPONSOR,
            SELF_PROMO,
            INTERACTION,
            INTRO,
            OUTRO,
            PREVIEW,
            FILLER,
            MUSIC_OFFTOPIC,
    };
    private static final Map<String, SegmentCategory> mValuesMap = new HashMap<>(2 * mValuesWithoutUnsubmitted.length);

    static {
        for (SegmentCategory value : mValuesWithoutUnsubmitted)
            mValuesMap.put(value.key, value);
    }

    @NonNull
    public static SegmentCategory[] valuesWithoutUnsubmitted() {
        return mValuesWithoutUnsubmitted;
    }

    @Nullable
    public static SegmentCategory byCategoryKey(@NonNull String key) {
        return mValuesMap.get(key);
    }

    @NonNull
    public final String key;
    @NonNull
    public final StringRef title;
    @NonNull
    public final StringRef description;

    /**
     * Skip button text, if the skip occurs in the first quarter of the video
     */
    @NonNull
    public final StringRef skipButtonTextBeginning;
    /**
     * Skip button text, if the skip occurs in the middle half of the video
     */
    @NonNull
    public final StringRef skipButtonTextMiddle;
    /**
     * Skip button text, if the skip occurs in the last quarter of the video
     */
    @NonNull
    public final StringRef skipButtonTextEnd;
    /**
     * Skipped segment toast, if the skip occurred in the first quarter of the video
     */
    @NonNull
    public final StringRef skippedToastBeginning;
    /**
     * Skipped segment toast, if the skip occurred in the middle half of the video
     */
    @NonNull
    public final StringRef skippedToastMiddle;
    /**
     * Skipped segment toast, if the skip occurred in the last quarter of the video
     */
    @NonNull
    public final StringRef skippedToastEnd;

    @NonNull
    public final Paint paint;
    public final int defaultColor;
    public int color;
    @NonNull
    public CategoryBehaviour behaviour;

    SegmentCategory(String key, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    CategoryBehaviour defaultBehavior, int defaultColor) {
        this(key, title, description,
                skipButtonText, skipButtonText, skipButtonText,
                skippedToastText, skippedToastText, skippedToastText,
                defaultBehavior, defaultColor);
    }

    SegmentCategory(String key, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    CategoryBehaviour defaultBehavior, int defaultColor) {
        this.key = Objects.requireNonNull(key);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.skipButtonTextBeginning = Objects.requireNonNull(skipButtonTextBeginning);
        this.skipButtonTextMiddle = Objects.requireNonNull(skipButtonTextMiddle);
        this.skipButtonTextEnd = Objects.requireNonNull(skipButtonTextEnd);
        this.skippedToastBeginning = Objects.requireNonNull(skippedToastBeginning);
        this.skippedToastMiddle = Objects.requireNonNull(skippedToastMiddle);
        this.skippedToastEnd = Objects.requireNonNull(skippedToastEnd);
        this.behaviour = Objects.requireNonNull(defaultBehavior);
        this.defaultColor = defaultColor;
        this.color = defaultColor;
        this.paint = new Paint();
        setColor(defaultColor);
    }

    public void setColor(int color) {
        color &= 0xFFFFFF;
        this.color = color;
        paint.setColor(color);
    }

    @NonNull
    public CharSequence getTitleWithDot() {
        return Html.fromHtml(String.format("<font color=\"#%06X\">⬤</font> %s", color, title));
    }

    /**
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return the skip button text
     */
    @NonNull
    public String getSkipButtonText(long segmentStartTime, long videoLength) {
        if (videoLength == 0) {
            return skipButtonTextBeginning.toString(); // video is still loading.  Assume it's the beginning
        }
        final float position = segmentStartTime / (float) videoLength;
        if (position < 0.25f) {
            return skipButtonTextBeginning.toString();
        } else if (position < 0.75f) {
            return skipButtonTextMiddle.toString();
        }
        return skipButtonTextEnd.toString();
    }

    /**
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return 'skipped segment' toast message
     */
    @NonNull
    public String getSkippedToastText(long segmentStartTime, long videoLength) {
        if (videoLength == 0) {
            return skippedToastBeginning.toString(); // video is still loading.  Assume it's the beginning
        }
        final float position = segmentStartTime / (float) videoLength;
        if (position < 0.25f) {
            return skippedToastBeginning.toString();
        } else if (position < 0.75f) {
            return skippedToastMiddle.toString();
        }
        return skippedToastEnd.toString();
    }
}
