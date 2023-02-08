package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour.MANUAL_SKIP;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentBehaviour.SKIP_AUTOMATICALLY;
import static app.revanced.integrations.sponsorblock.StringRef.sf;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

        // shield and voting button automatically show/hide themselves if feature is turned off

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
        SKIP_AUTOMATICALLY_ONCE("skip-once", 3, sf("skip_automatically_once"), true, true),
        SKIP_AUTOMATICALLY("skip", 2, sf("skip_automatically"), true, true),
        MANUAL_SKIP("manual-skip", 1, sf("skip_showbutton"), false, true),
        IGNORE("ignore", -1, sf("skip_ignore"), false, false);

        public final String key;
        public final int desktopKey;
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
            this.key = key;
            this.desktopKey = desktopKey;
            this.name = name;
            this.skip = skip;
            this.showOnTimeBar = showOnTimeBar;
        }

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
        SPONSOR("sponsor", sf("segments_sponsor"), sf("skip_button_text_sponsor"), sf("skipped_sponsor"), sf("segments_sponsor_sum"),
                SKIP_AUTOMATICALLY, 0xFF00d400),
        INTRO("intro", sf("segments_intermission"), sf("skip_button_text_intermission"), sf("skipped_intermission"), sf("segments_intermission_sum"),
                MANUAL_SKIP, 0xFF00ffff),
        OUTRO("outro", sf("segments_endcards"), sf("skip_button_text_endcard"), sf("skipped_endcard"), sf("segments_endcards_sum"),
                MANUAL_SKIP, 0xFF0202ed),
        INTERACTION("interaction", sf("segments_subscribe"), sf("skip_button_text_subscribe"), sf("skipped_subscribe"), sf("segments_subscribe_sum"),
                SKIP_AUTOMATICALLY, 0xFFcc00ff),
        SELF_PROMO("selfpromo", sf("segments_selfpromo"), sf("skip_button_text_selfpromo"), sf("skipped_selfpromo"), sf("segments_selfpromo_sum"),
                SKIP_AUTOMATICALLY, 0xFFffff00),
        MUSIC_OFFTOPIC("music_offtopic", sf("segments_nomusic"), sf("skip_button_text_nomusic"), sf("skipped_nomusic"), sf("segments_nomusic_sum"),
                MANUAL_SKIP, 0xFFff9900),
        PREVIEW("preview", sf("segments_preview"), sf("skip_button_text_preview"), sf("skipped_preview"), sf("segments_preview_sum"),
                DefaultBehaviour, 0xFF008fd6),
        FILLER("filler", sf("segments_filler"), sf("skip_button_text_filler"), sf("skipped_filler"), sf("segments_filler_sum"),
                DefaultBehaviour, 0xFF7300FF),
        UNSUBMITTED("unsubmitted", StringRef.empty, sf("skip_button_text_unsubmitted"), sf("skipped_unsubmitted"), StringRef.empty,
                SKIP_AUTOMATICALLY, 0xFFFFFFFF);

        private static final SegmentInfo[] mValuesWithoutUnsubmitted = new SegmentInfo[]{
                SPONSOR,
                INTRO,
                OUTRO,
                INTERACTION,
                SELF_PROMO,
                MUSIC_OFFTOPIC,
                PREVIEW,
                FILLER
        };
        private static final Map<String, SegmentInfo> mValuesMap = new HashMap<>(2 * values().length);

        static {
            for (SegmentInfo value : valuesWithoutUnsubmitted())
                mValuesMap.put(value.key, value);
        }

        public final String key;
        public final StringRef title;
        public final StringRef skipButtonText;
        public final StringRef skipMessage;
        public final StringRef description;
        public final Paint paint;
        public final int defaultColor;
        public int color;
        public SegmentBehaviour behaviour;

        SegmentInfo(String key,
                    StringRef title,
                    StringRef skipButtonText,
                    StringRef skipMessage,
                    StringRef description,
                    SegmentBehaviour behaviour,
                    int defaultColor) {
            this.key = key;
            this.title = title;
            this.skipButtonText = skipButtonText;
            this.skipMessage = skipMessage;
            this.description = description;
            this.behaviour = behaviour;
            this.defaultColor = defaultColor;
            this.color = defaultColor;
            this.paint = new Paint();
        }

        public static SegmentInfo[] valuesWithoutUnsubmitted() {
            return mValuesWithoutUnsubmitted;
        }

        public static SegmentInfo byCategoryKey(String key) {
            return mValuesMap.get(key);
        }

        public void setColor(int color) {
            color = color & 0xFFFFFF;
            this.color = color;
            paint.setColor(color);
            paint.setAlpha(255);
        }

        public CharSequence getTitleWithDot() {
            return Html.fromHtml(String.format("<font color=\"#%06X\">⬤</font> %s", color, title));
        }
    }
}
