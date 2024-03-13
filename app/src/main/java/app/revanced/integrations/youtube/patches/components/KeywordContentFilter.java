package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.ByteTrieSearch.convertStringsToBytes;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.LinkedHashSet;
import java.util.Set;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.ByteTrieSearch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * <pre>
 * Allows hiding home feed and search results based on keywords and/or channel names.
 *
 * Limitations:
 * - Searching for a keyword phrase will give no search results.
 *   This is because the buffer for each video contains the text the user searched for, and everything
 *   will be filtered away (even if that video title/channel does not contain any keywords).
 * - Filtering a channel name can still show Shorts from that channel in the search results.
 *   The most common Shorts layouts do not include the channel name, so they will not be filtered.
 * - Some layout component residue will remain, such as the video chapter previews for some search results.
 *   These components do not include the video title or channel name, and they
 *   appear outside the filtered components so they are not caught.
 * - Keywords are case sensitive, but some casing variation is manually added.
 *   (ie: "mr beast" automatically filters "Mr Beast" and "MR BEAST").
 */
@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
final class KeywordContentFilter extends Filter {

    /**
     * Minimum keyword/phrase length to prevent excessively broad content filtering.
     */
    private static final int MINIMUM_KEYWORD_LENGTH = 3;

    /**
     * Substrings that are always first in the path.
     */
    private final StringFilterGroup startsWithFilter = new StringFilterGroup(
            Settings.HIDE_KEYWORD_CONTENT,
            "home_video_with_context.eml",
            "search_video_with_context.eml",
            "related_video_with_context",
            "compact_video.eml",
            "inline_shorts",
            "shorts_video_cell",
            "shorts_pivot_item.eml");

    /**
     * Substrings that are never at the start of the path.
     */
    private final StringFilterGroup containsFilter = new StringFilterGroup(
            Settings.HIDE_KEYWORD_CONTENT,
            "modern_type_shelf_header_content.eml"
            // Part of 'shorts_shelf_carousel.eml' and usually shown to tablet layout.
            // For now, do not filter, as this is also used in the subscription feed
            // And this feature is presented as filtering only the home and search.
            // "shorts_lockup_cell.eml"
    );

    /**
     * The last value of {@link Settings#HIDE_KEYWORD_CONTENT_PHRASES}
     * parsed and loaded into {@link #bufferSearch}.
     * Used to allow changing the keywords without restarting the app.
     */
    private volatile String lastKeywordPhrasesParsed;

    private volatile ByteTrieSearch bufferSearch;

    /**
     * Change first letter of the first word to use title case.
     */
    private static String titleCaseFirstWordOnly(String sentence) {
        if (sentence.isEmpty()) {
            return sentence;
        }
        final int firstCodePoint = sentence.codePointAt(0);
        // In some non English languages title case is different than upper case.
        return new StringBuilder()
                .appendCodePoint(Character.toTitleCase(firstCodePoint))
                .append(sentence, Character.charCount(firstCodePoint), sentence.length())
                .toString();
    }

    /**
     * Uppercase the first letter of each word.
     */
    private static String capitalizeAllFirstLetters(String sentence) {
        if (sentence.isEmpty()) {
            return sentence;
        }
        final int delimiter = ' ';
        // Use code points and not characters to handle unicode surrogates.
        int[] codePoints = sentence.codePoints().toArray();
        boolean capitalizeNext = true;
        for (int i = 0, length = codePoints.length; i < length; i++) {
            final int codePoint = codePoints[i];
            if (codePoint == delimiter) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                codePoints[i] = Character.toUpperCase(codePoint);
                capitalizeNext = false;
            }
        }
        return new String(codePoints, 0, codePoints.length);
    }

    private synchronized void parseKeywords() { // Must be synchronized since Litho is multithreaded.
        String rawKeywords = Settings.HIDE_KEYWORD_CONTENT_PHRASES.get();
        if (rawKeywords == lastKeywordPhrasesParsed) {
            Logger.printDebug(() -> "Using previously initialized search");
            return; // Another thread won the race, and search is already initialized.
        }

        ByteTrieSearch search = new ByteTrieSearch();
        String[] split = rawKeywords.split("\n");
        if (split.length != 0) {
            // Linked Set so log statement are more organized and easier to read.
            Set<String> keywords = new LinkedHashSet<>(10 * split.length);

            for (String phrase : split) {
                // Remove any trailing white space the user may have accidentally included.
                phrase = phrase.stripTrailing();
                if (phrase.isBlank()) continue;

                if (phrase.length() < MINIMUM_KEYWORD_LENGTH) {
                    // Do not reset the setting. Keep the invalid keywords so the user can fix the mistake.
                    Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_length", MINIMUM_KEYWORD_LENGTH, phrase));
                    continue;
                }
                keywords.add(phrase);

                // Add common casing that might appear.
                //
                // This could be simplified by adding case insensitive search to the prefix search,
                // which is very simple to add to StringTreSearch for Unicode and ByteTrieSearch for ASCII.
                //
                // But to support Unicode with ByteTrieSearch would require major changes because
                // UTF-8 characters can be different byte lengths, which does
                // not allow comparing two different byte arrays using simple plain array indexes.
                //
                // Instead add all common case variations of the words.
                keywords.add(phrase.toLowerCase());
                keywords.add(titleCaseFirstWordOnly(phrase));
                keywords.add(capitalizeAllFirstLetters(phrase));
                keywords.add(phrase.toUpperCase());
            }

            search.addPatterns(convertStringsToBytes(keywords.toArray(new String[0])));
            Logger.printDebug(() -> "Search using: (" + search.getEstimatedMemorySize() + " KB) keywords: " + keywords);
        }

        bufferSearch = search;
        lastKeywordPhrasesParsed = rawKeywords; // Must set last.
    }

    public KeywordContentFilter() {
        // Keywords are parsed on first call to isFiltered()
        addPathCallbacks(startsWithFilter, containsFilter);
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex != 0 && matchedGroup == startsWithFilter) {
            return false;
        }
        // Field is intentionally compared using reference equality.
        if (Settings.HIDE_KEYWORD_CONTENT_PHRASES.get() != lastKeywordPhrasesParsed) {
            // User changed the keywords.
            parseKeywords();
        }
        if (!bufferSearch.matches(protobufBufferArray)) {
            return false;
        }
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}