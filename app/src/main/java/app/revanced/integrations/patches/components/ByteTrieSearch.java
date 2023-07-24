package app.revanced.integrations.patches.components;

import androidx.annotation.NonNull;

public class ByteTrieSearch extends TrieSearch<byte[]> {

    private static class ByteTrieNode extends TrieNode<byte[]> {
        TrieNode<byte[]> createNode() {
            return new ByteTrieNode();
        }
        char getCharValue(byte[] textToSearch, int index) {
            return (char) textToSearch[index];
        }
        int getLength(byte[] text) {
            return text.length;
        }
    }

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }

    @Override
    public void addPattern(@NonNull byte[] pattern) {
        super.addPattern(pattern, pattern.length);
    }

    @Override
    public void addPattern(@NonNull byte[] pattern, @NonNull TriePatternMatchedCallback<byte[]> callback) {
        super.addPattern(pattern, pattern.length, callback);
    }

    @Override
    public boolean matches(@NonNull byte[] textToSearch) {
        return super.matches(textToSearch, textToSearch.length);
    }

}
