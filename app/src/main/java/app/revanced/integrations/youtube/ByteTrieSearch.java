package app.revanced.integrations.youtube;

import androidx.annotation.NonNull;

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

    /**
     * @return If the pattern is valid to add to this instance.
     */
    public static boolean isValidPattern(byte[] pattern) {
        for (byte b : pattern) {
            if (TrieNode.isInvalidRange((char) b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for the common usage of converting Strings to raw bytes.
     */
    public static byte[][] convertStringsToBytes(@NonNull String[] strings) {
        final int length = strings.length;
        byte[][] replacement = new byte[length][];
        for (int i = 0; i < length; i++) {
            replacement[i] = strings[i].getBytes();
        }
        return replacement;
    }

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }
}
