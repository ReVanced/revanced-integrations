package app.revanced.integrations.patches.components;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Text pattern searching using a prefix tree (trie).
 */
public final class StringTrieSearch extends TrieSearch<String> {

    private static final class StringTrieNode extends TrieNode<String> {
        TrieNode<String> createNode() {
            return new StringTrieNode();
        }
        char getCharValue(String text, int index) {
            return text.charAt(index);
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
