package app.revanced.integrations.patches.components;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ByteTrieSearch;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringTrieSearch;
import app.revanced.integrations.utils.TrieSearch;

abstract class FilterGroup<T> {
    final static class FilterGroupResult {
        private final boolean filtered;
        private final SettingsEnum setting;

        public FilterGroupResult(final SettingsEnum setting, final boolean filtered) {
            this.setting = setting;
            this.filtered = filtered;
        }

        public SettingsEnum getSetting() {
            return setting;
        }

        public boolean isFiltered() {
            return filtered;
        }
    }

    protected final SettingsEnum setting;
    protected final T[] filters;

    /**
     * Initialize a new filter group.
     *
     * @param setting The associated setting.
     * @param filters The filters.
     */
    @SafeVarargs
    public FilterGroup(final SettingsEnum setting, final T... filters) {
        this.setting = setting;
        this.filters = filters;
    }

    public boolean isEnabled() {
        return setting == null || setting.getBoolean();
    }

    public abstract FilterGroupResult check(final T stack);
}

class StringFilterGroup extends FilterGroup<String> {

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public StringFilterGroup(final SettingsEnum setting, final String... filters) {
        super(setting, filters);
    }

    @Override
    public FilterGroupResult check(final String string) {
        return new FilterGroupResult(setting, string != null && ReVancedUtils.containsAny(string, filters));
    }
}

final class CustomFilterGroup extends StringFilterGroup {

    public CustomFilterGroup(final SettingsEnum setting, final SettingsEnum filter) {
        super(setting, filter.getString().split(","));
    }
}

/**
 * If you have more than 1 filter patterns, then all instances of
 * this class should filtered using a {@link ByteArrayFilterGroupList},
 * which uses a prefix tree to give better performance.
 */
class ByteArrayFilterGroup extends FilterGroup<byte[]> {

    private final int[][] failurePatterns;

    // Modified implementation from https://stackoverflow.com/a/1507813
    private static int indexOf(final byte[] data, final byte[] pattern, final int[] failure) {
        // Finds the first occurrence of the pattern in the byte array using
        // KMP matching algorithm.
        int patternLength = pattern.length;
        for (int i = 0, j = 0, dataLength = data.length; i < dataLength; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == patternLength) {
                return i - patternLength + 1;
            }
        }
        return -1;
    }

    private static int[] createFailurePattern(byte[] pattern) {
        // Computes the failure function using a boot-strapping process,
        // where the pattern is matched against itself.
        final int patternLength = pattern.length;
        final int[] failure = new int[patternLength];

        for (int i = 1, j = 0; i < patternLength; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    public ByteArrayFilterGroup(SettingsEnum setting, byte[]... filters) {
        super(setting, filters);
        failurePatterns = new int[filters.length][];
        int i = 0;
        for (byte[] pattern : filters) {
            failurePatterns[i++] = createFailurePattern(pattern);
        }
    }

    @Override
    public FilterGroupResult check(final byte[] bytes) {
        var matched = false;
        for (int i = 0, length = filters.length; i < length; i++) {
            if (indexOf(bytes, filters[i], failurePatterns[i]) >= 0) {
                matched = true;
                break;
            }
        }

        return new FilterGroupResult(setting, matched);
    }
}


final class ByteArrayAsStringFilterGroup extends ByteArrayFilterGroup {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ByteArrayAsStringFilterGroup(SettingsEnum setting, String... filters) {
        super(setting, Arrays.stream(filters).map(String::getBytes).toArray(byte[][]::new));
    }
}

abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {
    private final List<T> filterGroups = new ArrayList<>();
    /**
     * Search graph. Created only if needed.
     */
    private TrieSearch<V> search;

    @SafeVarargs
    protected final void addAll(final T... groups) {
        filterGroups.addAll(Arrays.asList(groups));
        search = null; // Rebuild, if already created.
    }

    protected final void buildSearch() {
        LogHelper.printDebug(() -> "Creating prefix search tree");
        search = createSearchGraph();
        for (T group : filterGroups) {
            for (V pattern : group.filters) {
                search.addPattern(pattern, (matchedStartIndex, callbackParameter)
                        -> group.isEnabled());
            }
        }
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return filterGroups.iterator();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void forEach(@NonNull Consumer<? super T> action) {
        filterGroups.forEach(action);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Spliterator<T> spliterator() {
        return filterGroups.spliterator();
    }

    protected boolean contains(final V stack) {
        if (search == null) {
            buildSearch(); // Lazy load the search
        }
        return search.matches(stack);
    }

    protected abstract TrieSearch<V> createSearchGraph();
}

final class StringFilterGroupList extends FilterGroupList<String, StringFilterGroup> {
    protected StringTrieSearch createSearchGraph() {
        return new StringTrieSearch();
    }
}

final class ByteArrayFilterGroupList extends FilterGroupList<byte[], ByteArrayFilterGroup> {
    protected ByteTrieSearch createSearchGraph() {
        return new ByteTrieSearch();
    }
}

abstract class Filter {
    /**
     * All group filters must be set before the constructor call completes.
     * Otherwise {@link #isFiltered(String, String, byte[], FilterGroupList, FilterGroup)}
     * will never be called for any matches.
     */
    protected final StringFilterGroupList pathFilterGroups = new StringFilterGroupList();
    protected final StringFilterGroupList identifierFilterGroups = new StringFilterGroupList();
    protected final ByteArrayFilterGroupList protobufBufferFilterGroups = new ByteArrayFilterGroupList();

    /**
     * Called after a filter has been matched (both when enabled and disabled).
     * Default implementation is to filter the matched item if the matchedGroup is enabled.
     * Subclasses can perform additional or different checks if needed.
     *
     * Method is called off the main thread.
     *
     * @param matchedList  The matchedGroup matchedList the filter belongs to.
     * @param matchedGroup The actual filter that matched.
     * @return True if the litho item should be hidden.
     */
    @SuppressWarnings("rawtypes")
    boolean isFiltered(final String path, final String identifier, final byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup) {
        final boolean isEnabled = matchedGroup.isEnabled();

        if (isEnabled && SettingsEnum.DEBUG.getBoolean()) {
            if (pathFilterGroups == matchedList) {
                LogHelper.printDebug(() -> getClass().getSimpleName() + " Filtered path: " + path);
            } else if (identifierFilterGroups == matchedList) {
                LogHelper.printDebug(() -> getClass().getSimpleName() + " Filtered identifier: " + identifier);
            } else if (protobufBufferFilterGroups == matchedList) {
                LogHelper.printDebug(() -> getClass().getSimpleName() + " Filtered from protobuf-buffer");
            }
        }

        return isEnabled;
    }
}

@RequiresApi(api = Build.VERSION_CODES.N)
@SuppressWarnings("unused")
public final class LithoFilterPatch {
    /**
     * Simple wrapper to pass the litho parameters thru the prefix search.
     */
    private static final class LithoFilterParameters {
        public final String path;
        public final String identifier;
        public final byte[] protobuffer;

        LithoFilterParameters(StringBuilder lithoPath, String lithoIdentifier, ByteBuffer protobuffer) {
            this.path = lithoPath.toString();
            this.identifier = lithoIdentifier;
            this.protobuffer = protobuffer.array();
        }

        @NonNull
        @Override
        public String toString() {
            // Estimated percentage of the buffer that are Strings.
            StringBuilder builder = new StringBuilder(protobuffer.length / 4);
            builder.append( "ID: ");
            builder.append(identifier);
            builder.append(" Path: ");
            builder.append(path);
            // TODO: allow turning on/off proto buffer logging with a debug setting?
            builder.append(" BufferStrings: ");
            findAsciiStrings(builder, protobuffer);

            return builder.toString();
        }

        /**
         * Search thru a byte array for all ASCII strings.
         */
        private static void findAsciiStrings(StringBuilder builder, byte[] buffer) {
            // Valid ASCII values (ignore control characters).
            final int minimumAscii = 32;  // 32 = space character
            final int maximumAscii = 126; // 127 = delete character
            final int minimumAsciiStringLength = 4; // Minimum length of an ASCII string to include.
            String delimitingCharacter = "❙"; // Non ascii character, to allow easier log filtering.

            int asciiStartIndex = -1;
            int stringLength = 0;
            for (int i = 0, length = buffer.length; i < length; i++) {
                char character = (char) buffer[i];
                if (character < minimumAscii || maximumAscii < character) { // Not a letter, number, or symbol.
                    if (asciiStartIndex >= 0) {
                        if (i - asciiStartIndex >= minimumAsciiStringLength) {
                            builder.append(delimitingCharacter);
                            for (int j = asciiStartIndex; j < i; j++) {
                                builder.append((char) buffer[j]);
                            }
                            builder.append(delimitingCharacter).append(' ');
                        }
                        asciiStartIndex = -1;
                    }
                    continue;
                }
                if (asciiStartIndex < 0) {
                    asciiStartIndex = i;
                }
            }
            if (asciiStartIndex >= 0) {
                builder.append(delimitingCharacter);
            }
        }
    }

    private static final Filter[] filters = new Filter[] {
            new DummyFilter() // Replaced by patch.
    };

    private static final StringTrieSearch pathSearchTree = new StringTrieSearch();
    private static final StringTrieSearch identifierSearchTree = new StringTrieSearch();
    private static final ByteTrieSearch protoSearchTree = new ByteTrieSearch();

    static {
        for (Filter filter : filters) {
            addFilterToSearchTree(pathSearchTree, filter, filter.pathFilterGroups);
            addFilterToSearchTree(identifierSearchTree, filter, filter.identifierFilterGroups);
            addFilterToSearchTree(protoSearchTree, filter, filter.protobufBufferFilterGroups);
        }

        LogHelper.printDebug(() -> "Using: "
                + pathSearchTree.getPatterns().size() + " path filters"
                + " (" + pathSearchTree.getEstimatedMemorySize() + " KB), "
                + identifierSearchTree.getPatterns().size() + " identifier filters"
                + " (" + identifierSearchTree.getEstimatedMemorySize() + " KB), "
                + protoSearchTree.getPatterns().size() + " buffer filters"
                + " (" + protoSearchTree.getEstimatedMemorySize() + " KB)");
    }

    private static <T> void addFilterToSearchTree(TrieSearch<T> pathSearchTree,
                                                  Filter filter, FilterGroupList<T, ? extends FilterGroup<T>> list) {
        for (FilterGroup<T> group : list) {
            for (T pattern : group.filters) {
                pathSearchTree.addPattern(pattern, (matchedStartIndex, callbackParameter) -> {
                            LithoFilterParameters parameters = (LithoFilterParameters) callbackParameter;
                            return filter.isFiltered(parameters.path, parameters.identifier, parameters.protobuffer, list, group);
                        }
                );
            }
        }
    }

    /**
     * Injection point.  Called off the main thread.
     */
    @SuppressWarnings("unused")
    public static boolean filter(@NonNull StringBuilder pathBuilder, @Nullable String lithoIdentifier,
                                 @NonNull ByteBuffer protobufBuffer) {
        try {
            // It is assumed that protobufBuffer is empty as well in this case.
            if (pathBuilder.length() == 0)
                return false;

            LithoFilterParameters parameter = new LithoFilterParameters(pathBuilder, lithoIdentifier, protobufBuffer);
            LogHelper.printDebug(() -> "Searching " + parameter);

            if (pathSearchTree.matches(parameter.path, parameter)) return true;
            if (parameter.identifier != null) {
                if (identifierSearchTree.matches(parameter.identifier, parameter)) return true;
            }
            if (protoSearchTree.matches(parameter.protobuffer, parameter)) return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Litho filter failure", ex);
        }

        return false;
    }
}