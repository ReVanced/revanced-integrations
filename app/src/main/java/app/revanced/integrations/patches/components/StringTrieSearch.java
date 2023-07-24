package app.revanced.integrations.patches.components;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Text pattern searching using a prefix tree (trie).
 * Can significantly speed up searching for multiple patterns.
 */
public class StringTrieSearch extends TrieSearch<String> {

    private static class StringTrieNode extends TrieNode<String> {
        TrieNode<String> createNode() {
            return new StringTrieNode();
        }
        char getCharValue(String textToSearch, int index) {
            return textToSearch.charAt(index);
        }
        int getLength(String text) {
            return text.length();
        }
    }

    public StringTrieSearch() {
        super(new StringTrieNode());
    }

    @Override
    public void addPattern(@NonNull String pattern) {
        super.addPattern(pattern, pattern.length());
    }

    @Override
    public void addPattern(@NonNull String pattern, @NonNull TriePatternMatchedCallback<String> callback) {
        super.addPattern(pattern, pattern.length(), Objects.requireNonNull(callback));
    }

    @Override
    public boolean matches(@NonNull String textToSearch) {
        return super.matches(textToSearch, textToSearch.length());
    }
}
