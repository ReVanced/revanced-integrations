package app.revanced.integrations.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class ByteTrieSearch extends TrieSearch<byte[]> {

    private static final class ByteTrieNode extends TrieNode<byte[]> {
        ByteTrieNode() {
            super();
        }
        ByteTrieNode(char nodeCharacterValue) {
            super(nodeCharacterValue);
        }
        @Override
        TrieNode<byte[]> createNode(char nodeCharacterValue) {
            return new ByteTrieNode(nodeCharacterValue);
        }
        @Override
        char getCharValue(byte[] text, int index) {
            return (char) text[index];
        }
        @Override
        int getTextLength(byte[] text) {
            return text.length;
        }
    }

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }
}
