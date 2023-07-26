package app.revanced.integrations.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class ByteTrieSearch extends TrieSearch<ByteBuffer> {

    private static final class ByteTrieNode extends TrieNode<ByteBuffer> {
        TrieNode<ByteBuffer> createNode() {
            return new ByteTrieNode();
        }
        char getCharValue(ByteBuffer text, int index) {
            return text.getChar(index);
        }
    }

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }

    @Override
    public void addPattern(@NonNull ByteBuffer pattern) {
        super.addPattern(pattern, pattern.position(), pattern.limit(), null);
    }

    @Override
    public void addPattern(@NonNull ByteBuffer pattern, @NonNull TriePatternMatchedCallback<ByteBuffer> callback) {
        super.addPattern(pattern, pattern.position(), pattern.limit(), Objects.requireNonNull(callback));
    }

    @Override
    public boolean matches(@NonNull ByteBuffer textToSearch,  @Nullable Object callbackParameter) {
        return super.matches(textToSearch, textToSearch.position(), textToSearch.limit(), callbackParameter);
    }

}
