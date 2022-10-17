package app.revanced.integrations.patches;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

/**
 * Helper functions.
 */
final class Extensions {
    static boolean containsAny(final String value, final String... targets) {
        for (String string : targets)
            if (value.contains(string)) return true;
        return false;
    }

    static boolean any(LithoBlockRegister register, String path) {
        for (var rule : register) {
            if (!rule.isEnabled()) continue;

            var result = rule.check(path);
            if (result.isBlocked()) {
                return true;
            }
        }

        return false;
    }

    static int indexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return 0;
        }

        for (int i = 0; i < array.length - target.length + 1; i++) {
            boolean targetFound = true;
            for (int j = 0; j < target.length; j++) {
                if (array[i+j] != target[j]) {
                    targetFound = false;
                    break;
                }
            }
            if (targetFound) {
                return i;
            }
        }
        return -1;
    }
}

final class BlockRule {
    final static class BlockResult {
        private final boolean blocked;
        private final SettingsEnum setting;

        public BlockResult(final SettingsEnum setting, final boolean blocked) {
            this.setting = setting;
            this.blocked = blocked;
        }

        public SettingsEnum getSetting() {
            return setting;
        }

        public boolean isBlocked() {
            return blocked;
        }
    }

    private final SettingsEnum setting;
    private final String[] blocks;

    /**
     * Initialize a new rule for components.
     *
     * @param setting The setting which controls the blocking of this component.
     * @param blocks  The rules to block the component on.
     */
    public BlockRule(final SettingsEnum setting, final String... blocks) {
        this.setting = setting;
        this.blocks = blocks;
    }

    public String[] getBlocks() {
        return blocks;
    }

    public boolean isEnabled() {
        return setting.getBoolean();
    }

    public BlockResult check(final String string) {
        return new BlockResult(setting, string != null && Extensions.containsAny(string, blocks));
    }
}

abstract class Filter {
    final LithoBlockRegister register = new LithoBlockRegister();

    abstract boolean filter(final String path, final String identifier, final ByteBuffer buffer);
}

final class LithoBlockRegister implements Iterable<BlockRule> {
    private final ArrayList<BlockRule> blocks = new ArrayList<>();

    public void registerAll(BlockRule... blocks) {
        this.blocks.addAll(Arrays.asList(blocks));
    }

    @NonNull
    @Override
    public Iterator<BlockRule> iterator() {
        return blocks.iterator();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void forEach(@NonNull Consumer<? super BlockRule> action) {
        blocks.forEach(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Spliterator<BlockRule> spliterator() {
        return blocks.spliterator();
    }
}

public final class LithoFilterPatch {
    private static final Filter[] filters = new Filter[]{
            new GeneralBytecodeAdsPatch(),
            new ButtonsPatch()
    };

    public static boolean filter(final StringBuilder pathBuilder, final String identifier, final ByteBuffer buffer) {
        var path = pathBuilder.toString();
        if (path.isEmpty()) return false;

        LogHelper.debug(LithoFilterPatch.class, String.format("Searching (ID: %s): %s", identifier, path));

        for (var filter : filters) {
            if (filter.filter(path, identifier, buffer)) return true;
        }

        return false;
    }
}

final class ButtonsPatch extends Filter {
    public static BlockRule actionBar = new BlockRule(SettingsEnum.HIDE_ACTION_BAR, "video_action_bar");

    public static BlockRule like = new BlockRule(SettingsEnum.HIDE_LIKE_BUTTON, "CellType|ScrollableContainerType|ContainerType|ContainerType|like_button");
    public static BlockRule dislike = new BlockRule(SettingsEnum.HIDE_DISLIKE_BUTTON, "CellType|ScrollableContainerType|ContainerType|ContainerType|dislike_button");
    public static BlockRule live_chat = new BlockRule(SettingsEnum.HIDE_LIVE_CHAT_BUTTON, "yt_outline_message_bubble_overlap");
    public static BlockRule share = new BlockRule(SettingsEnum.HIDE_SHARE_BUTTON, "yt_outline_share");
    public static BlockRule report = new BlockRule(SettingsEnum.HIDE_REPORT_BUTTON, "yt_outline_flag");
    public static BlockRule shorts = new BlockRule(SettingsEnum.HIDE_SHORTS_BUTTON, "yt_outline_youtube_shorts_plus");
    public static BlockRule thanks = new BlockRule(SettingsEnum.HIDE_THANKS_BUTTON, "yt_outline_dollar_sign_heart");
    public static BlockRule clip = new BlockRule(SettingsEnum.HIDE_CLIP_BUTTON, "yt_outline_scissors");
    public static BlockRule download = new BlockRule(SettingsEnum.HIDE_DOWNLOAD_BUTTON, "CellType|ScrollableContainerType|ContainerType|ContainerType|download_button");
    public static BlockRule playlist = new BlockRule(SettingsEnum.HIDE_PLAYLIST_BUTTON, "CellType|ScrollableContainerType|ContainerType|ContainerType|save_to_playlist_button");

    public ButtonsPatch() {
        this.register.registerAll(
            like,
            dislike,
            live_chat,
            share,
            report,
            shorts,
            thanks,
            clip,
            download,
            playlist
        );
    }

    private boolean hideActionButtons(final String path, final ByteBuffer buffer) {
        for (BlockRule rule : register) {
            if (rule.isEnabled()) {
                if (Extensions.containsAny(path, actionBar.getBlocks()[0])) {
                    if (rule.check(path).isBlocked()) return true;

                    if (Extensions.containsAny(path, "ContainerType|ContainerType|video_action_button")) {
                        int bufferIndex = Extensions.indexOf(buffer.array(), rule.getBlocks()[0].getBytes());

                        if (bufferIndex > 0 && bufferIndex < 2000)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean filter(final String path, final String identifier, final ByteBuffer buffer) {
        if (actionBar.isEnabled()) {
            if (actionBar.check(identifier).isBlocked()) return true;
        }
        else
        {
            return hideActionButtons(path, buffer);
        }

        return false;
    }
}

class GeneralBytecodeAdsPatch extends Filter {
    private final BlockRule identifierBlock;

    public GeneralBytecodeAdsPatch() {
        var comments = new BlockRule(SettingsEnum.ADREMOVER_COMMENTS_REMOVAL, "comments_");
        var communityPosts = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL, "post_base_wrapper");
        var communityGuidelines = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL, "community_guidelines");
        var compactBanner = new BlockRule(SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL, "compact_banner");
        var inFeedSurvey = new BlockRule(SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL, "in_feed_survey");
        var medicalPanel = new BlockRule(SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL, "medical_panel");
        var paidContent = new BlockRule(SettingsEnum.ADREMOVER_PAID_CONTECT_REMOVAL, "paid_content_overlay");
        var merchandise = new BlockRule(SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL, "product_carousel");
        var shorts = new BlockRule(SettingsEnum.ADREMOVER_SHORTS_SHELF_REMOVAL, "shorts_shelf");
        var infoPanel = new BlockRule(SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL, "publisher_transparency_panel", "single_item_information_panel");
        var suggestions = new BlockRule(SettingsEnum.ADREMOVER_SUGGESTIONS_REMOVAL, "horizontal_video_shelf");
        var latestPosts = new BlockRule(SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS, "post_shelf");
        var channelGuidelines = new BlockRule(SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES, "channel_guidelines_entry_banner");
        var generalAds = new BlockRule(
            SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
            // could be required
            //"full_width_square_image_layout",
            "video_display_full_buttoned_layout",
            "_ad",
            "ad_",
            "ads_video_with_context",
            "cell_divider",
            "reels_player_overlay",
            "shelf_header",
            "watch_metadata_app_promo",
            "video_display_full_layout"
        );
        var movieAds = new BlockRule(
            SettingsEnum.ADREMOVER_MOVIE_REMOVAL,
            "browsy_bar",
            "compact_movie",
            "horizontal_movie_shelf",
            "movie_and_show_upsell_card"
        );

        this.register.registerAll(
            generalAds,
            communityPosts,
            paidContent,
            shorts,
            suggestions,
            latestPosts,
            movieAds,
            comments,
            communityGuidelines,
            compactBanner,
            inFeedSurvey,
            medicalPanel,
            merchandise,
            infoPanel,
            channelGuidelines
        );

        // Block for the ComponentContext.identifier field
        identifierBlock = new BlockRule(SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL, "carousel_ad");
    }

    public boolean filter(final String path, final String identifier, final ByteBuffer _buffer) {
        // Do not block on these
        if (Extensions.containsAny(path,
            "home_video_with_context",
            "related_video_with_context",
            "search_video_with_context",
            "download_",
            "library_recent_shelf",
            "menu",
            "root",
            "-count",
            "-space",
            "-button"
        )) return false;

        for (var rule : register) {
            if (!rule.isEnabled()) continue;

            var result = rule.check(path);
            if (result.isBlocked()) {
                LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocked: " + path);
                return true;
            }
        }

        if (identifierBlock.check(identifier).isBlocked()) {
            LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocked: " + identifier);
            return true;
        }
        return false;
    }
}
