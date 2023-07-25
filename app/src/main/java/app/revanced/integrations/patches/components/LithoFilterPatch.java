package app.revanced.integrations.patches.components;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

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

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public CustomFilterGroup(final SettingsEnum setting, final SettingsEnum filter) {
        super(setting, filter.getString().split(","));
    }
}

class ByteArrayFilterGroup extends FilterGroup<byte[]> {

    private final int[][] failurePatterns;

    // Modified implementation from https://stackoverflow.com/a/1507813
    private int indexOf(final byte[] data, final byte[] pattern, final int[] failure) {
        if (data.length == 0)
            return -1;

        // Finds the first occurrence of the pattern in the byte array using
        // KMP matching algorithm.
        for (int i = 0, j = 0, dataLength = data.length; i < dataLength; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
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

    /**
     * {@link FilterGroup#FilterGroup(SettingsEnum, Object[])}
     */
    public ByteArrayFilterGroup(final SettingsEnum setting, final byte[]... filters) {
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
            if (indexOf(bytes, filters[i], failurePatterns[i]) == -1)
                continue;

            matched = true;
            break;
        }

        return new FilterGroupResult(setting, matched);
    }
}

final class ByteArrayAsStringFilterGroup extends ByteArrayFilterGroup {

    /**
     * {@link ByteArrayFilterGroup#ByteArrayFilterGroup(SettingsEnum, byte[]...)}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ByteArrayAsStringFilterGroup(SettingsEnum setting, String... filters) {
        super(setting, Arrays.stream(filters).map(String::getBytes).toArray(byte[][]::new));
    }
}

abstract class FilterGroupList<V, T extends FilterGroup<V>> implements Iterable<T> {
    private final ArrayList<T> filterGroups = new ArrayList<>();

    @SafeVarargs
    protected final void addAll(final T... filterGroups) {
        this.filterGroups.addAll(Arrays.asList(filterGroups));
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
        for (T filterGroup : this) {
            if (!filterGroup.isEnabled())
                continue;

            var result = filterGroup.check(stack);
            if (result.isFiltered()) {
                return true;
            }
        }

        return false;
    }
}

final class StringFilterGroupList extends FilterGroupList<String, StringFilterGroup> {
}

final class ByteArrayFilterGroupList extends FilterGroupList<byte[], ByteArrayFilterGroup> {
}

abstract class Filter {
    /*
     * All group filters must be set before the constructor call completes.
     * Otherwise the filter will never be called when the group matches.
     */
    final protected StringFilterGroupList pathFilterGroups = new StringFilterGroupList();
    final protected StringFilterGroupList identifierFilterGroups = new StringFilterGroupList();
    final protected ByteArrayFilterGroupList protobufBufferFilterGroups = new ByteArrayFilterGroupList();

    /**
     * Called after a filter has been matched.
     * Default implementation is to filter the matched item if the matchedGroup is enabled.
     * Subclasses can perform additional checks if needed.
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
    private static final Filter[] filters = new Filter[] {
            new DummyFilter() // Replaced by patch.
    };

    private static final StringTrieSearch pathSearchTree = new StringTrieSearch();
    private static final StringTrieSearch identifierSearchTree = new StringTrieSearch();
    private static final ByteTrieSearch protoSearchTree = new ByteTrieSearch();

    private static String path;
    private static String identifier;
    private static byte[] protobufBufferArray;

    static {
        for (Filter filter : filters) {
            for (StringFilterGroup group : filter.pathFilterGroups) {
                addFilterToSearchTree(pathSearchTree, filter, filter.pathFilterGroups, group);
            }
            for (StringFilterGroup group : filter.identifierFilterGroups) {
                addFilterToSearchTree(identifierSearchTree, filter, filter.identifierFilterGroups, group);
            }
            for (ByteArrayFilterGroup group : filter.protobufBufferFilterGroups) {
                addFilterToSearchTree(protoSearchTree, filter, filter.protobufBufferFilterGroups, group);
            }
        }

        LogHelper.printDebug(() -> "Using: " + pathSearchTree.getPatterns().size() + " path, "
                + identifierSearchTree.getPatterns().size() + " identifier, "
                + protoSearchTree.getPatterns().size() + " litho filters");
    }

    @SuppressWarnings("rawtypes")
    private static <T> void addFilterToSearchTree(TrieSearch<T> pathSearchTree,
                                                  Filter filter, FilterGroupList list, FilterGroup<T> group) {
        for (T pattern : group.filters) {
            pathSearchTree.addPattern(pattern, (TrieSearch.TriePatternMatchedCallback<T>)
                    (searchText, matchedStartIndex, matchedEndIndex) ->
                            filter.isFiltered(path, identifier, protobufBufferArray, list, group)
            );
        }
    }

    /**
     * Injection point.  Called off the main thread.
     */
    @SuppressWarnings("unused")
    public static boolean filter(@NonNull StringBuilder pathBuilder, @Nullable String lithoIdentifier,
                                 @NonNull ByteBuffer protobufBuffer) {
        try {
            path = pathBuilder.toString();

            // It is assumed that protobufBuffer is empty as well in this case.
            if (path.isEmpty())
                return false;

            identifier = lithoIdentifier;

            LogHelper.printDebug(() -> String.format(
                    "Searching (ID: %s, Buffer-size: %s): %s",
                    identifier, protobufBuffer.remaining(), path));

            protobufBufferArray = protobufBuffer.array();

            if (pathSearchTree.matches(path)) return true;
            if (identifier != null && identifierSearchTree.matches(identifier)) return true;
            if (protoSearchTree.matches(protobufBufferArray)) return true;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Litho filter failure", ex);
        }

        return false;
    }
}