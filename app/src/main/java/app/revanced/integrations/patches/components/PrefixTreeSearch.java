package app.revanced.integrations.patches.components;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Text pattern searching using a prefix tree (trie).
 * Can significantly speed up searching for multiple strings.
 *
 * If only 1 pattern is added, this falls back to using a simple
 * linear text search (which is slightly faster since there's no graph to traverse).
 */
public class PrefixTreeSearch {

    private static class PrefixNode {
        static final int CHAR_RANGE = 128; // Support ASCII range.
        PrefixNode[] children;
        /**
         * If any pattern ends at this node.
         */
        boolean isEndOfPattern;

        void addPattern(@NonNull String pattern) {
            addPattern(pattern, 0, pattern.length());
        }

        /**
         * @param pattern Pattern to add.
         * @param patternIndex Current recursive index of the pattern.
         * @param patternLength Length of the pattern.
         */
        private void addPattern(@NonNull String pattern, int patternIndex, int patternLength) {
            if (patternIndex == patternLength) {
                isEndOfPattern = true; // Reached the end of the string.
                return;
            }
            if (isEndOfPattern) {
                // A previous pattern has ended here,
                // and there's no need to build out any further as matching will stop here.
                return;
            }
            if (children == null) {
                children = new PrefixNode[CHAR_RANGE];
            }
            final char character = pattern.charAt(patternIndex);
            PrefixNode child = children[character];
            if (child == null) {
                child = new PrefixNode();
                children[character] = child;
            }
            child.addPattern(pattern, patternIndex + 1, patternLength);
        }

        /**
         * @param searchText Text to search for patterns in.
         * @param searchTextIndex Current recursive search text index.
         * @param searchTextLength Length of the search text.
         * @return If any pattern matches.
         */
        boolean matches(String searchText, int searchTextIndex, int searchTextLength) {
            if (isEndOfPattern) return true; // A pattern has matched.

            if (searchTextIndex == searchTextLength) {
                return false; // Reached end of search range and found no matches.
            }
            final char character = searchText.charAt(searchTextIndex);
            PrefixNode child = children[character];
            if (child == null) {
                return false;
            }
            return child.matches(searchText, searchTextIndex + 1, searchTextLength);
        }
    }

    /**
     * If the number if patterns is less than this, than regular linear search is used (which is usually faster).
     */
    private static final int MIN_PATTERNS_TO_USE_TRIE = 3;

    /**
     * Root node, and it's children represent the first pattern characters.
     */
    private final PrefixNode root = new PrefixNode();

    /**
     * Patterns to match.
     */
    private final List<String> patterns = new ArrayList<>();

    /**
     * The shortest pattern ever added.
     */
    private int minPatternLength;

    public void addPattern(@NonNull String pattern) {
        final int patternLength = pattern.length();
        if (patternLength == 0) return; // Nothing to match

        patterns.add(pattern);
        final int numPatternsAdded = patterns.size();
        if (numPatternsAdded < MIN_PATTERNS_TO_USE_TRIE) {
            return; // Don't build tree until minimum is reached.
        } else if (numPatternsAdded == MIN_PATTERNS_TO_USE_TRIE) {
            // Build the tree with previously added patterns.
            for (String existingPattern : patterns) {
                final int existingLength = existingPattern.length();
                minPatternLength = (minPatternLength == 0)
                        ? existingLength
                        : Math.min(minPatternLength, existingLength);
                root.addPattern(existingPattern);
            }
            return;
        }

        minPatternLength = Math.min(minPatternLength, patternLength);
        root.addPattern(pattern);
    }

    /**
     * Combine instances.
     */
    public void addOtherPrefixTree(@NonNull PrefixTreeSearch other) {
        for (String pattern : other.patterns) {
            addPattern(pattern);
        }
    }

    public boolean matches(@NonNull String textToSearch) {
        if (patterns.size() < MIN_PATTERNS_TO_USE_TRIE) {
            // Use simple text search as it's slightly faster.
            for (String pattern : patterns) {
                if (textToSearch.contains(pattern)) return true;
            }
            return false;
        }

        final int searchTextLength = textToSearch.length();
        // Can stop searching when the remaining search text is shorter than the shortest pattern.
        final int searchMaxStartIndex = searchTextLength - minPatternLength;
        for (int i = 0; i <= searchMaxStartIndex; i++) {
            if (root.matches(textToSearch, i, searchTextLength)) return true;
        }
        return false;
    }

    public List<String> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

}
