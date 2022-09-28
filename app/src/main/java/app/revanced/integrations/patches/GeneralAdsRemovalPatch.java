package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeneralAdsRemovalPatch {
    //Used by app.revanced.patches.youtube.ad.general.patch.GeneralAdsRemovalPatch
    public static void hidePromotedVideoItem(View view) {
        if (SettingsEnum.GENERAL_ADS_REMOVAL.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }

    //Used by app.revanced.patches.youtube.ad.general.patch.GeneralAdsRemovalPatch
    public static boolean containsAd(String template, String inflatedTemplate, ByteBuffer buffer) {
        return containsLithoAd(template, inflatedTemplate, buffer);
    }

    private static boolean containsLithoAd(String template, String inflatedTemplate, ByteBuffer buffer) {
        String readableBuffer = new String(buffer.array(), StandardCharsets.UTF_8);

        //ADS
        if (template != null && !template.trim().isEmpty()) {
            LogHelper.debug(GeneralAdsRemovalPatch.class, "Searching for AD: " + template);

            List<String> blockList = new ArrayList<>();
            List<String> bufferBlockList = new ArrayList<>();

            if (SettingsEnum.GENERAL_ADS_REMOVAL.getBoolean()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
                blockList.add("cell_divider");
                blockList.add("reels_player_overlay");
                blockList.add("shelf_header");
                blockList.add("text_search_ad_with_description_first");
                blockList.add("watch_metadata_app_promo");
                blockList.add("video_display_full_layout");

                bufferBlockList.add("ad_cpn");
            }
            if (SettingsEnum.SUGGESTED_FOR_YOU_REMOVAL.getBoolean()) {
                bufferBlockList.add("watch-vrecH");
            }
            if (SettingsEnum.MOVIE_REMOVAL.getBoolean()) {
                blockList.add("browsy_bar");
                blockList.add("compact_movie");
                blockList.add("horizontal_movie_shelf");
                blockList.add("movie_and_show_upsell_card");

                bufferBlockList.add("YouTube Movies");
            }
            if (containsAny(template, "home_video_with_context", "related_video_with_context") &&
                    anyMatch(bufferBlockList, readableBuffer::contains)
            ) return true;

            if (SettingsEnum.COMMENTS_REMOVAL.getBoolean()) {
                blockList.add("comments_");
            }
            if (SettingsEnum.COMMUNITY_GUIDELINES.getBoolean()) {
                blockList.add("community_guidelines");
            }
            if (SettingsEnum.COMPACT_BANNER_REMOVAL.getBoolean()) {
                blockList.add("compact_banner");
            }
            if (SettingsEnum.EMERGENCY_BOX_REMOVAL.getBoolean()) {
                blockList.add("emergency_onebox");
            }
            if (SettingsEnum.FEED_SURVEY_REMOVAL.getBoolean()) {
                blockList.add("in_feed_survey");
            }
            if (SettingsEnum.MEDICAL_PANEL_REMOVAL.getBoolean()) {
                blockList.add("medical_panel");
            }
            if (SettingsEnum.PAID_CONTECT_REMOVAL.getBoolean()) {
                blockList.add("paid_content_overlay");
            }
            if (SettingsEnum.COMMUNITY_POSTS_REMOVAL.getBoolean()) {
                blockList.add("post_base_wrapper");
            }
            if (SettingsEnum.MERCHANDISE_REMOVAL.getBoolean()) {
                blockList.add("product_carousel");
            }
            if (SettingsEnum.SHORTS_SHELF.getBoolean()) {
                blockList.add("shorts_shelf");
            }
            if (SettingsEnum.INFO_PANEL_REMOVAL.getBoolean()) {
                blockList.add("publisher_transparency_panel");
                blockList.add("single_item_information_panel");
            }
            if (SettingsEnum.HIDE_SUGGESTIONS.getBoolean()) {
                blockList.add("horizontal_video_shelf");
            }
            if (SettingsEnum.HIDE_LATEST_POSTS.getBoolean()) {
                blockList.add("post_shelf");
            }
            if (SettingsEnum.HIDE_CHANNEL_GUIDELINES.getBoolean()) {
                blockList.add("channel_guidelines_entry_banner");
            }

            if (containsAny(template,
                    "home_video_with_context",
                    "related_video_with_context",
                    "search_video_with_context",
                    "menu",
                    "root",
                    "-count",
                    "-space",
                    "-button"
            )) return false;

            if (anyMatch(blockList, template::contains)) {
                LogHelper.debug(GeneralAdsRemovalPatch.class, "Blocking ad: " + template);
                return true;
            }

            if (SettingsEnum.DEBUG.getBoolean()) {
                if (template.contains("related_video_with_context")) {
                    LogHelper.debug(GeneralAdsRemovalPatch.class, template + " | " + bytesToHex(buffer.array()));
                    return false;
                }
                LogHelper.debug(GeneralAdsRemovalPatch.class, template + " returns false.");
            }
        }

        //Action Bar Buttons
        List<String> actionButtonsBlockList = new ArrayList<>();

        if (SettingsEnum.HIDE_PLAYER_LIVE_CHAT_BUTTON.getBoolean()) {
            actionButtonsBlockList.add("yt_outline_message_bubble_overlap");
        }
        if (SettingsEnum.HIDE_PLAYER_REPORT_BUTTON.getBoolean()) {
            actionButtonsBlockList.add("yt_outline_flag");
        }
        if (SettingsEnum.HIDE_PLAYER_CREATE_SHORT_BUTTON.getBoolean()) {
            actionButtonsBlockList.add("yt_outline_youtube_shorts_plus");
        }
        if (SettingsEnum.HIDE_PLAYER_THANKS_BUTTON.getBoolean()) {
            actionButtonsBlockList.add("yt_outline_dollar_sign_heart");
        }
        if (SettingsEnum.HIDE_PLAYER_CREATE_CLIP_BUTTON.getBoolean()) {
            actionButtonsBlockList.add("yt_outline_scissors");
        }

        if (containsAny(inflatedTemplate, "ContainerType|ContainerType|video_action_button") &&
            anyMatch(actionButtonsBlockList, readableBuffer::contains)) {
            return true;
        }

        if (SettingsEnum.HIDE_PLAYER_DOWNLOAD_BUTTON.getBoolean() &&
            containsAny(inflatedTemplate, "ContainerType|ContainerType|download_button")) {
            return true;
        }

        //Comments Teasers
        List<String> commentsTeaserBlockList = new ArrayList<>();

        if (SettingsEnum.HIDE_PLAYER_SPOILER_COMMENT.getBoolean()) {
            commentsTeaserBlockList.add("ContainerType|comments_entry_point_teaser");
        }
        if (SettingsEnum.HIDE_PLAYER_EXTERNAL_COMMENT_BOX.getBoolean()) {
            commentsTeaserBlockList.add("ContainerType|comments_entry_point_simplebox");
        }

        if (anyMatch(commentsTeaserBlockList, inflatedTemplate::contains)) {
            return true;
        }

        return false;
    }

    private static boolean containsAny(String value, String... targets) {
        for (String string : targets)
            if (value.contains(string)) return true;
        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }

    private static <T> boolean anyMatch(List<T> value, APredicate<? super T> predicate) {
        for (T t : value) {
            if (predicate.test(t)) return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface APredicate<T> {
        boolean test(T t);
    }
}
