package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.StringTrieSearch;

@SuppressWarnings("unused")
final class DescriptionComponentsFilter extends Filter {

    private final StringTrieSearch exceptions = new StringTrieSearch();

    private final StringFilterGroup macroMarkersCarousel;
    private final ByteArrayFilterGroup chaptersSectionBufferGroup;
    private final ByteArrayFilterGroup keyConceptsSectionBufferGroup;

    public DescriptionComponentsFilter() {
        exceptions.addPatterns(
                "compact_channel",
                "description",
                "grid_video",
                "inline_expander",
                "metadata"
        );

        final StringFilterGroup attributesSection = new StringFilterGroup(
                Settings.HIDE_ATTRIBUTES_SECTION,
                "gaming_section",
                "music_section",
                "video_attributes_section"
        );

        final StringFilterGroup infoCardsSection = new StringFilterGroup(
                Settings.HIDE_INFO_CARDS_SECTION,
                "infocards_section"
        );

        final StringFilterGroup podcastSection = new StringFilterGroup(
                Settings.HIDE_PODCAST_SECTION,
                "playlist_section"
        );

        final StringFilterGroup transcriptSection = new StringFilterGroup(
                Settings.HIDE_TRANSCRIPT_SECTION,
                "transcript_section"
        );

        macroMarkersCarousel = new StringFilterGroup(
                null,
                "macro_markers_carousel.eml"
        );

        chaptersSectionBufferGroup = new ByteArrayFilterGroup(
                Settings.HIDE_CHAPTERS_SECTION,
                "chapters_horizontal_shelf"
        );

        keyConceptsSectionBufferGroup = new ByteArrayFilterGroup(
                Settings.HIDE_KEY_CONCEPTS_SECTION,
                "learning_concept_macro_markers_carousel_shelf"
        );

        addPathCallbacks(
                attributesSection,
                infoCardsSection,
                podcastSection,
                transcriptSection,
                macroMarkersCarousel
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == macroMarkersCarousel) {
            if (contentIndex == 0
                    && chaptersSectionBufferGroup.check(protobufBufferArray).isFiltered()
                    && keyConceptsSectionBufferGroup.check(protobufBufferArray).isFiltered()) {
                return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }

            return false;
        }

        return super.isFiltered(path, identifier, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}