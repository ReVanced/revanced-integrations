package app.revanced.integrations.patches.components;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Searches for a group of different patterns using a trie (prefix tree).
 * Can significantly speed up searching for multiple patterns.
 */
public abstract class TrieSearch<T> {

    interface TriePatternMatchedCallback<T> {
        /**
         * Called when a pattern is matched.
         *
         * @param searchText        The text that was searched.
         * @param matchedStartIndex Start index of the search text, where the pattern was matched
         * @param matchedEndIndex   Exclusive end index of the search text, where the pattern was matched.
         * @return True, if the search should stop here.
         *         If false, searching will continue to look for other matches.
         */
        boolean patternMatched(T searchText, int matchedStartIndex, int matchedEndIndex);
    }

    protected static abstract class TrieNode<T> {
        static final int CHAR_RANGE = 128; // Support only ASCII range.
        TrieNode<T>[] children;
        /**
         * Callbacks for all patterns that end at this node.
         */
        @Nullable
        List<TriePatternMatchedCallback<T>> endOfPatternCallback;

        void addPattern(@NonNull T pattern, @Nullable TriePatternMatchedCallback<T> callback) {
            addPattern(pattern, getLength(pattern), 0, callback);
        }

        /**
         * @param pattern       Pattern to add.
         * @param patternLength Length of the pattern.
         * @param patternIndex  Current recursive index of the pattern.
         * @param callback      Callback, where a value of NULL indicates to always accept a pattern match.
         */
        private void addPattern(@NonNull T pattern, int patternLength, int patternIndex,
                                @Nullable TriePatternMatchedCallback<T> callback) {
            if (patternIndex == patternLength) { // Reached the end of the string.
                if (endOfPatternCallback == null) {
                    endOfPatternCallback = new ArrayList<>(1);
                }
                endOfPatternCallback.add(callback);
                return;
            }
            if (children == null) {
                children = new TrieNode[CHAR_RANGE];
            }
            final char character = getCharValue(pattern, patternIndex);
            if (character >= CHAR_RANGE) {
                throw new IllegalArgumentException();
            }
            TrieNode<T> child = children[character];
            if (child == null) {
                child = createNode();
                children[character] = child;
            }
            child.addPattern(pattern, patternLength, patternIndex + 1, callback);
        }

        /**
         * @param searchText         Text to search for patterns in.
         * @param searchTextLength   Length of the search text.
         * @param searchTextIndex    Current recursive search text index.  Also the end index of the current pattern match.
         * @param currentMatchLength current search depth, and also the length of the current pattern match.
         * @return If any pattern matches and it's associated callback halted the search.
         */
        private boolean matches(T searchText, int searchTextLength, int searchTextIndex, int currentMatchLength) {
            if (endOfPatternCallback != null) {
                final int matchStartIndex = searchTextIndex - currentMatchLength;
                for (@Nullable TriePatternMatchedCallback<T> callback : endOfPatternCallback) {
                    if (callback == null || callback.patternMatched(searchText, matchStartIndex, searchTextIndex)) {
                        return true; // Callback confirms the match.
                    }
                }
                if (children == null) {
                    return false; // Reached a graph end point, and there's no further patterns to search.
                }
            }

            if (searchTextIndex == searchTextLength) {
                return false; // Reached end of the search text and found no matches.
            }
            final char character = getCharValue(searchText, searchTextIndex);
            if (character >= CHAR_RANGE) {
                return false; // Non ascii character
            }
            TrieNode<T> child = children[character];
            if (child == null) {
                return false;
            }
            return child.matches(searchText, searchTextLength, searchTextIndex + 1,
                    currentMatchLength + 1);
        }

        abstract TrieNode<T> createNode();
        abstract char getCharValue(T text, int index);
        abstract int getLength(T text);
    }


    /**
     * Root node, and it's children represent the first pattern characters.
     */
    private final TrieNode<T> root;

    /**
     * Patterns to match.
     */
    private final List<T> patterns = new ArrayList<>();

    protected TrieSearch(@NonNull TrieNode<T> root) {
        this.root = Objects.requireNonNull(root);
    }

    @SafeVarargs
    public final void addPatterns(@NonNull T... patterns) {
        for (T pattern : patterns) {
            addPattern(pattern);
        }
    }

    protected void addPattern(@NonNull T pattern, int patternLength) {
        addPattern(pattern, patternLength, null);
    }

    protected void addPattern(@NonNull T pattern, int patternLength, @Nullable TriePatternMatchedCallback<T> callback) {
        if (patternLength == 0) return; // Nothing to match

        patterns.add(pattern);
        root.addPattern(pattern, callback);
    }

    protected boolean matches(@NonNull T textToSearch, int textToSearchLength) {
        if (patterns.size() == 0) {
            return false; // No patterns were added.
        }
        for (int i = 0; i < textToSearchLength; i++) {
            if (root.matches(textToSearch, textToSearchLength, i, 0)) return true;
        }
        return false;
    }

    public int numberOfPatterns() {
        return patterns.size();
    }

    public List<T> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    /**
     * Adds a pattern that will always return a positive match if found.
     *
     * @param pattern Pattern to add. Calling this with a zero length pattern does nothing.
     */
    public abstract void addPattern(@NonNull T pattern);

    /**
     * @param pattern  Pattern to add. Calling this with an empty pattern (zero length) does nothing.
     * @param callback Callback to determine if searching should halt and a match is found.
     */
    public abstract void addPattern(@NonNull T pattern, @NonNull TriePatternMatchedCallback<T> callback);

    /**
     * Searches thru text, looking for any substring that matches any pattern in this tree.
     *
     * @param textToSearch Text to search thru.
     * @return If any pattern matched, and it's callback halted searching.
     */
    public abstract boolean matches(@NonNull T textToSearch);
}
