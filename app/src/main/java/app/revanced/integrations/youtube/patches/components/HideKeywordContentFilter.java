package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.ByteTrieSearch.convertStringsToBytes;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.StringSetting;
import app.revanced.integrations.youtube.ByteTrieSearch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * <pre>
 * Allows hiding home feed and search results based on keywords and/or channel names.
 *
 * Limitations:
 * - Searching for a keyword phrase will give no search results
 * - Filtering a channel name can still show Shorts from that channel in the search results
 * - Some layout component residue will remain, such as the video chapter previews for some search results.
 * - Keywords are case sensitive, but some casing variation is manually added.
 *   (such as "Mr Beast" automatically filtering "mr beast" and "MR BEAST").
 */
@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
final class HideKeywordContentFilter extends Filter {

    /**
     * Substrings that are always first in the path.
     */
    private final StringFilterGroup startsWithFilter = new StringFilterGroup(
            Settings.HIDE_KEYWORD_CONTENT,
            "home_video_with_content.eml",
            "search_video_with_context.eml",
            "related_video_with_content",
            "inline_shorts",
            "shorts_video_cell",
            "shorts_pivot_item.eml");

    /**
     * Substrings that are never at the start of the path.
     */
    private final StringFilterGroup containsFilter = new StringFilterGroup(
            Settings.HIDE_KEYWORD_CONTENT,
            "modern_type_shelf_header_content.eml");

    private final ByteTrieSearch bufferSearch = new ByteTrieSearch();

    /**
     * Minimum keyword/phrase length to prevent excessively broad content filtering.
     */
    private static final int MINIMUM_KEYWORD_LENGTH = 3;

    private static List<String> splitAndVerifyStrings(StringSetting setting) {
        String[] split = setting.get().split("\n");
        List<String> keywords = new ArrayList<>(split.length);

        for (String phrase : split) {
            // Remove any trailing white space the user may have accidentally included.
            phrase = phrase.stripTrailing();
            if (phrase.isBlank()) continue;

            if (phrase.length() < MINIMUM_KEYWORD_LENGTH) {
                Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_length", MINIMUM_KEYWORD_LENGTH, phrase));
                continue;
            }
            keywords.add(phrase);
        }

        return keywords;
    }

    private void parseKeywords() {
        List<String> keywords = splitAndVerifyStrings(Settings.HIDE_KEYWORD_CONTENT_PHRASES);
        if (keywords.isEmpty()) {
            return;
        }

        // Add common casing that might appear.
        // If a desired keyword has mixed casing (such as "Matt Whatever")
        // the user will need to add that exact string.
        // This could be simplified by adding case insensitive search to the prefix search,
        // but that also brings a small performance hit.
        List<String> modifiedKeywords = new ArrayList<>();
        for (String keyword : keywords) {
            modifiedKeywords.add(keyword); // Original casing as added by the user

            // Add lower case and upper case variants.
            String lowerCase = keyword.toLowerCase();
            if (!keyword.equals(lowerCase)) {
                modifiedKeywords.add(lowerCase);
            }
            String upperCase = keyword.toUpperCase();
            if (!keyword.equals(upperCase)) {
                modifiedKeywords.add(upperCase);
            }
            // Include first letter capitalization variant.
            String sentenceCapital = Character.toUpperCase(keyword.charAt(0))
                    + keyword.substring(1);
            if (!sentenceCapital.equals(keyword) && !sentenceCapital.equals(upperCase)) {
                modifiedKeywords.add(sentenceCapital);
            }
        }
        Logger.printDebug(() -> "Using keywords: " + modifiedKeywords);
        bufferSearch.addPatterns(convertStringsToBytes(modifiedKeywords.toArray(new String[0])));
    }

    public HideKeywordContentFilter() {
        parseKeywords();

        if (bufferSearch.numberOfPatterns() != 0) {
            // Add the callbacks only if there are keywords to search.
            addPathCallbacks(startsWithFilter, containsFilter);
        }
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex != 0 && matchedGroup == startsWithFilter) {
            return false;
        }
        if (!bufferSearch.matches(protobufBufferArray)) {
            return false;
        }
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}