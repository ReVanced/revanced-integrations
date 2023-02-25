package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour.MANUAL_SKIP;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour.SKIP_AUTOMATICALLY;
import static app.revanced.integrations.sponsorblock.StringRef.sf;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class SponsorBlockSettings {

    public static final String CATEGORY_COLOR_SUFFIX = "_color";
    public static final SegmentBehaviour DefaultBehaviour = SegmentBehaviour.IGNORE;
    public static String sponsorBlockUrlCategories = "[]";

    public static void update() {
        ReVancedUtils.verifyOnMainThread();
        LogHelper.printDebug(() -> "updating SponsorBlockSettings");
        SharedPreferences preferences = SharedPrefHelper.getPreferences(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK);

        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            SponsorBlockView.hideSkipButton();
            SponsorBlockView.hideNewSegmentLayout();
            PlayerController.setCurrentVideoId(null);
        }
        if (!SettingsEnum.SB_NEW_SEGMENT_ENABLED.getBoolean()) {
            SponsorBlockView.hideNewSegmentLayout();
        }

        // shield and voting button automatically show/hide themselves if feature is turned on/off

        SegmentBehaviour[] possibleBehaviours = SegmentBehaviour.values();
        final ArrayList<String> enabledCategories = new ArrayList<>(possibleBehaviours.length);
        for (SegmentInfo segment : SegmentInfo.values()) {
            String categoryColor = preferences.getString(segment.key + CATEGORY_COLOR_SUFFIX, SponsorBlockUtils.formatColorString(segment.defaultColor));
            segment.setColor(Color.parseColor(categoryColor));

            SegmentBehaviour behaviour = null;
            String value = preferences.getString(segment.key, null);
            if (value != null) {
                for (SegmentBehaviour possibleBehaviour : possibleBehaviours) {
                    if (possibleBehaviour.key.equals(value)) {
                        behaviour = possibleBehaviour;
                        break;
                    }
                }
            }
            if (behaviour != null) {
                segment.behaviour = behaviour;
            } else {
                behaviour = segment.behaviour;
            }

            if (behaviour.showOnTimeBar && segment != SegmentInfo.UNSUBMITTED)
                enabledCategories.add(segment.key);
        }

        //"[%22sponsor%22,%22outro%22,%22music_offtopic%22,%22intro%22,%22selfpromo%22,%22interaction%22,%22preview%22]";
        if (enabledCategories.isEmpty())
            sponsorBlockUrlCategories = "[]";
        else
            sponsorBlockUrlCategories = "[%22" + TextUtils.join("%22,%22", enabledCategories) + "%22]";

        String uuid = SettingsEnum.SB_UUID.getString();
        if (uuid == null || uuid.length() == 0) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            SettingsEnum.SB_UUID.saveValue(uuid);
        }
    }

    public enum SegmentBehaviour {
        SKIP_AUTOMATICALLY("skip", 2, sf("sb_skip_automatically"), true, true),
        // desktop does not have skip-once behavior. Key is unique to ReVanced
        SKIP_AUTOMATICALLY_ONCE("skip-once", 3, sf("sb_skip_automatically_once"), true, true),
        MANUAL_SKIP("manual-skip", 1, sf("sb_skip_showbutton"), false, true),
        SHOW_IN_SEEKBAR("seekbar-only", 0, sf("sb_skip_seekbaronly"), false, true),
        IGNORE("ignore", -1, sf("sb_skip_ignore"), false, false);

        @NonNull
        public final String key;
        public final int desktopKey;
        @NonNull
        public final StringRef name;
        /**
         * If the segment should skip automatically
         */
        public final boolean skip;
        public final boolean showOnTimeBar;

        SegmentBehaviour(String key,
                         int desktopKey,
                         StringRef name,
                         boolean skip,
                         boolean showOnTimeBar) {
            this.key = Objects.requireNonNull(key);
            this.desktopKey = desktopKey;
            this.name = Objects.requireNonNull(name);
            this.skip = skip;
            this.showOnTimeBar = showOnTimeBar;
        }

        @Nullable
        public static SegmentBehaviour byDesktopKey(int desktopKey) {
            for (SegmentBehaviour behaviour : values()) {
                if (behaviour.desktopKey == desktopKey) {
                    return behaviour;
                }
            }
            return null;
        }
    }

    public enum SegmentInfo {
        SPONSOR("sponsor", sf("sb_segments_sponsor"), sf("sb_segments_sponsor_sum"), sf("sb_skip_button_sponsor"), sf("sb_skipped_sponsor"),
                SKIP_AUTOMATICALLY, 0xFF00d400),
        SELF_PROMO("selfpromo", sf("sb_segments_selfpromo"), sf("sb_segments_selfpromo_sum"), sf("sb_skip_button_selfpromo"), sf("sb_skipped_selfpromo"),
                SKIP_AUTOMATICALLY, 0xFFffff00),
        INTERACTION("interaction", sf("sb_segments_interaction"), sf("sb_segments_interaction_sum"), sf("sb_skip_button_interaction"), sf("sb_skipped_interaction"),
                SKIP_AUTOMATICALLY, 0xFFcc00ff),
        INTRO("intro", sf("sb_segments_intro"), sf("sb_segments_intro_sum"),
                sf("sb_skip_button_intro_beginning"), sf("sb_skip_button_intro_middle"), sf("sb_skip_button_intro_end"),
                sf("sb_skipped_intro_beginning"), sf("sb_skipped_intro_middle"), sf("sb_skipped_intro_end"),
                MANUAL_SKIP, 0xFF00ffff),
        OUTRO("outro", sf("sb_segments_outro"), sf("sb_segments_outro_sum"), sf("sb_skip_button_outro"), sf("sb_skipped_outro"),
                MANUAL_SKIP, 0xFF0202ed),
        PREVIEW("preview", sf("sb_segments_preview"), sf("sb_segments_preview_sum"),
                sf("sb_skip_button_preview_beginning"), sf("sb_skip_button_preview_middle"), sf("sb_skip_button_preview_end"),
                sf("sb_skipped_preview_beginning"), sf("sb_skipped_preview_middle"), sf("sb_skipped_preview_end"),
                DefaultBehaviour, 0xFF008fd6),
        FILLER("filler", sf("sb_segments_filler"), sf("sb_segments_filler_sum"), sf("sb_skip_button_filler"), sf("sb_skipped_filler"),
                DefaultBehaviour, 0xFF7300FF),
        MUSIC_OFFTOPIC("music_offtopic", sf("sb_segments_nomusic"), sf("sb_segments_nomusic_sum"), sf("sb_skip_button_nomusic"), sf("sb_skipped_nomusic"),
                MANUAL_SKIP, 0xFFff9900),
        UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("sb_skip_button_unsubmitted"), sf("sb_skipped_unsubmitted"),
                SKIP_AUTOMATICALLY, 0xFFFFFFFF);

        private static final SegmentInfo[] mValuesWithoutUnsubmitted = new SegmentInfo[]{
                SPONSOR,
                SELF_PROMO,
                INTERACTION,
                INTRO,
                OUTRO,
                PREVIEW,
                FILLER,
                MUSIC_OFFTOPIC,
        };
        private static final Map<String, SegmentInfo> mValuesMap = new HashMap<>(2 * values().length);

        static {
            for (SegmentInfo value : valuesWithoutUnsubmitted())
                mValuesMap.put(value.key, value);
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
        public SegmentBehaviour behaviour;

        SegmentInfo(String key, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    SegmentBehaviour behaviour, int defaultColor) {
            this(key, title, description,
                    skipButtonText, skipButtonText, skipButtonText,
                    skippedToastText, skippedToastText, skippedToastText,
                    behaviour, defaultColor);
        }
        SegmentInfo(String key, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    SegmentBehaviour behaviour, int defaultColor) {
            this.key = Objects.requireNonNull(key);
            this.title = Objects.requireNonNull(title);
            this.description = Objects.requireNonNull(description);
            this.skipButtonTextBeginning = Objects.requireNonNull(skipButtonTextBeginning);
            this.skipButtonTextMiddle = Objects.requireNonNull(skipButtonTextMiddle);
            this.skipButtonTextEnd = Objects.requireNonNull(skipButtonTextEnd);
            this.skippedToastBeginning = Objects.requireNonNull(skippedToastBeginning);
            this.skippedToastMiddle = Objects.requireNonNull(skippedToastMiddle);
            this.skippedToastEnd = Objects.requireNonNull(skippedToastEnd);
            this.behaviour = Objects.requireNonNull(behaviour);
            this.defaultColor = defaultColor;
            this.color = defaultColor;
            this.paint = new Paint();
        }

        @NonNull
        public static SegmentInfo[] valuesWithoutUnsubmitted() {
            return mValuesWithoutUnsubmitted;
        }

        @Nullable
        public static SegmentInfo byCategoryKey(@NonNull String key) {
            return mValuesMap.get(key);
        }

        public void setColor(int color) {
            color = color & 0xFFFFFF;
            this.color = color;
            paint.setColor(color);
            paint.setAlpha(255);
        }

        @NonNull
        public CharSequence getTitleWithDot() {
            return Html.fromHtml(String.format("<font color=\"#%06X\">⬤</font> %s", color, title));
        }

        /**
         * @param segmentStartTime video time the segment category started
         * @param videoLength length of the video
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
         * @param videoLength length of the video
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
}
