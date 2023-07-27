package app.revanced.integrations.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Searches for a group of different patterns using a trie (prefix tree).
 * Can significantly speed up searching for multiple patterns.
 *
 * Currently only supports ASCII non-control characters (letters/numbers/symbols).
 * But could be modified to also support UTF-8 unicode.
 */
public abstract class TrieSearch<T> {

    public interface TriePatternMatchedCallback<T> {
        /**
         * Called when a pattern is matched.
         *
         * @param matchedStartIndex Start index of the search text, where the pattern was matched.
         * @param callbackParameter Optional parameter passed into {@link TrieSearch#matches(Object, Object)}.
         * @return True, if the search should stop here.
         *         If false, searching will continue to look for other matches.
         */
        boolean patternMatched(int matchedStartIndex, Object callbackParameter);
    }

    protected static abstract class TrieNode<T> {
        // Support only ASCII letters/numbers/symbols and filter out all control characters.
        static final char MIN_VALID_CHAR = 32; // Space character.
        static final char MAX_VALID_CHAR = 126; // 127 = delete character.
        static final int NUMBER_OF_CHILDREN = MAX_VALID_CHAR - MIN_VALID_CHAR + 1;

        private static boolean isInvalidRange(char character) {
            return character < MIN_VALID_CHAR || character > MAX_VALID_CHAR;
        }

        TrieNode<T>[] children;
        /**
         * Callbacks for all patterns that end at this node.
         */
        @Nullable
        List<TriePatternMatchedCallback<T>> endOfPatternCallback;

        /**
         * @param pattern       Pattern to add.
         * @param patternLength Length of the pattern.
         * @param patternIndex  Current recursive index of the pattern.
         * @param callback      Callback, where a value of NULL indicates to always accept a pattern match.
         */
        void addPattern(@NonNull T pattern, int patternLength, int patternIndex,
                        @Nullable TriePatternMatchedCallback<T> callback) {
            if (patternIndex == patternLength) { // Reached the end of the string.
                if (endOfPatternCallback == null) {
                    endOfPatternCallback = new ArrayList<>(1);
                }
                endOfPatternCallback.add(callback);
                return;
            }
            if (children == null) {
                children = new TrieNode[NUMBER_OF_CHILDREN];
            }
            char character = getCharValue(pattern, patternIndex);
            if (isInvalidRange(character)) {
                throw new IllegalArgumentException("invalid character at index " + patternIndex + ": " + pattern);
            }
            character -= MIN_VALID_CHAR; // Adjust to the array range.
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
        private boolean matches(T searchText, int searchTextLength, int searchTextIndex, int currentMatchLength,
                                Object callbackParameter) {
            if (endOfPatternCallback != null) {
                final int matchStartIndex = searchTextIndex - currentMatchLength;
                for (@Nullable TriePatternMatchedCallback<T> callback : endOfPatternCallback) {
                    if (callback == null) {
                        return true; // No callback, and all matches are valid.
                    }
                    if (callback.patternMatched(matchStartIndex, callbackParameter)) {
                        return true; // Callback confirmed the match.
                    }
                }
                if (children == null) {
                    return false; // Reached a graph end point, and there's no further patterns to search.
                }
            }

            if (searchTextIndex == searchTextLength) {
                return false; // Reached end of the search text and found no matches.
            }
            char character = getCharValue(searchText, searchTextIndex);
            if (isInvalidRange(character)) {
                return false; // Not an ASCII letter/number/symbol.
            }
            character -= MIN_VALID_CHAR; // Adjust to the array range.
            TrieNode<T> child = children[character];
            if (child == null) {
                return false;
            }
            return child.matches(searchText, searchTextLength, searchTextIndex + 1,
                    currentMatchLength + 1, callbackParameter);
        }

        /**
         * @return Estimated number of memory pointers used, starting from this node and including all children.
         */
        protected int estimatedNumberOfPointersUsed() {
            // Number of fields in this class (callback list and empty children pointer).
            final int numberOfFieldsInClass = 2;
            if (children == null) {
                return numberOfFieldsInClass;
            }
            int numChildArrays = NUMBER_OF_CHILDREN + numberOfFieldsInClass;
            for (TrieNode<T> child : children) {
                if (child != null) {
                    numChildArrays += child.estimatedNumberOfPointersUsed();
                }
            }
            return numChildArrays;
        }

        abstract TrieNode<T> createNode();
        abstract char getCharValue(T text, int index);
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

    protected void addPattern(@NonNull T pattern, int patternLength, @Nullable TriePatternMatchedCallback<T> callback) {
        if (patternLength == 0) return; // Nothing to match

        patterns.add(pattern);
        root.addPattern(pattern, patternLength, 0, callback);
    }

    protected boolean matches(@NonNull T textToSearch, int textToSearchLength, @Nullable Object callbackParameter) {
        if (patterns.size() == 0) {
            return false; // No patterns were added.
        }
        for (int i = 0; i < textToSearchLength; i++) {
            if (root.matches(textToSearch, textToSearchLength, i, 0, callbackParameter)) return true;
        }
        return false;
    }

    /**
     * @return Estimated memory size (in kilobytes) of this instance.
     */
    public int getEstimatedMemorySize() {
        // Assume the device has less than 32GB of ram (and can use pointer compression),
        // or the device is 32-bit.
        final int numberOfBytesPerPointer = 4;
        // This ignores the memory size of object garbage collection entries,
        // and ignores the leaf node 1 element callback function arraylist.
        return (numberOfBytesPerPointer * root.estimatedNumberOfPointersUsed()) / 1024;
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
     * @param pattern  Pattern to add. Calling this with a zero length pattern does nothing.
     * @param callback Callback to determine if searching should halt when a match is found.
     */
    public abstract void addPattern(@NonNull T pattern, @NonNull TriePatternMatchedCallback<T> callback);

    /**
     * Searches thru text, looking for any substring that matches any pattern in this tree.
     *
     * @param textToSearch Text to search thru.
     * @param callbackParameter Optional parameter passed to the callbacks.
     * @return If any pattern matched, and it's callback halted searching.
     */
    public abstract boolean matches(@NonNull T textToSearch, @Nullable Object callbackParameter);

    /**
     * Identical to {@link #matches(Object, Object)} but with a null callback parameter.
     */
    public final boolean matches(@NonNull T textToSearch) {
        return matches(textToSearch, null);
    }
}
