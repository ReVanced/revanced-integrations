package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.shared.StringRef.str;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.ByteTrieSearch;
import app.revanced.integrations.youtube.StringTrieSearch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * Allows custom filtering using a path and optionally a proto buffer string.
 */
@SuppressWarnings("unused")
final class CustomFilter extends Filter {

    private static void showInvalidCharactersToast(@NonNull String expression) {
        Utils.showToastLong(str("revanced_custom_filter_toast_invalid_characters", expression));
    }

    private static void showInvalidSyntaxToast(@NonNull String expression) {
        Utils.showToastLong(str("revanced_custom_filter_toast_invalid_syntax", expression));
    }

    private static class CustomFilterGroup extends StringFilterGroup {
        /**
         * Optional character for the path that indicates the custom filter path must match the start.
         * Must be the first character of the expression.
         */
        public static final String SYNTAX_STARTS_WITH = "^";

        /**
         * Optional character that separates the path from a proto buffer string pattern.
         */
        public static final String SYNTAX_BUFFER_DELIMITER = "$";

        /**
         * @return the parsed objects, or NULL if there was a parse error.
         */
        @Nullable
        static Collection<CustomFilterGroup> parseCustomFilterGroups() {
            String rawCustomFilterText =  Settings.CUSTOM_FILTER_STRINGS.get();
            if (rawCustomFilterText.isBlank()) {
                return Collections.emptyList();
            }

            // Key is path including special characters (^ and/or $)
            Map<String, CustomFilterGroup> result = new HashMap<>();

            for (String expression : rawCustomFilterText.split("\n")) {
                final int indexOfBufferDelimiter = expression.indexOf(SYNTAX_BUFFER_DELIMITER);
                final boolean hasBufferStrings = indexOfBufferDelimiter >= 0;
                final boolean pathStartsWith = expression.startsWith(SYNTAX_STARTS_WITH);
                final int pathEndIndex = hasBufferStrings ? indexOfBufferDelimiter : expression.length();

                final String mapKey = expression.substring(0, pathEndIndex);
                final String path = pathStartsWith ? mapKey.substring(SYNTAX_STARTS_WITH.length()) : mapKey;
                if (path.isEmpty()) {
                    showInvalidSyntaxToast(expression);
                    return null;
                }
                if (!StringTrieSearch.isValidPattern(path)) {
                    // Currently only ASCII is allowed.
                    showInvalidCharactersToast(path);
                    return null;
                }

                // Use one group object for all expressions with the same path.
                // This ensures the buffer is searched exactly once
                // when multiple paths are used with different buffer strings.
                CustomFilterGroup group = result.get(mapKey);
                if (group == null) {
                    group = new CustomFilterGroup(pathStartsWith, path);
                    result.put(mapKey, group);
                }

                if (hasBufferStrings) {
                    if (indexOfBufferDelimiter == 0
                            || (pathStartsWith && indexOfBufferDelimiter == SYNTAX_STARTS_WITH.length())) {
                        // Expression has no path.
                        showInvalidSyntaxToast(expression);
                        return null;
                    }
                    String bufferString = expression.substring(
                            indexOfBufferDelimiter + SYNTAX_BUFFER_DELIMITER.length());
                    if (bufferString.isBlank()) {
                        showInvalidSyntaxToast(expression);
                        return null;
                    }
                    if (!StringTrieSearch.isValidPattern(bufferString)) {
                        showInvalidCharactersToast(bufferString);
                        return null;
                    }
                    group.addBufferString(bufferString);
                }
            }

            return result.values();
        }

        final boolean startsWith;
        ByteTrieSearch bufferSearch;

        CustomFilterGroup(boolean startsWith, @NonNull String path) {
            super(Settings.CUSTOM_FILTER, path);
            this.startsWith = startsWith;
        }

        void addBufferString(@NonNull String bufferString) {
            if (bufferSearch == null) {
                bufferSearch = new ByteTrieSearch();
            }
            bufferSearch.addPattern(bufferString.getBytes());
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CustomFilterGroup{");
            builder.append("path=");
            if (startsWith) builder.append(SYNTAX_STARTS_WITH);
            builder.append(filters[0]);

            if (bufferSearch != null) {
                String delimitingCharacter = "❙";
                builder.append(", bufferStrings=");
                builder.append(delimitingCharacter);
                for (byte[] bufferString : bufferSearch.getPatterns()) {
                    builder.append(new String(bufferString));
                    builder.append(delimitingCharacter);
                }
            }
            builder.append("}");
            return builder.toString();
        }
    }

    public CustomFilter() {
        Collection<CustomFilterGroup> groups = CustomFilterGroup.parseCustomFilterGroups();
        if (groups == null) {
            Settings.CUSTOM_FILTER_STRINGS.resetToDefault();
            Utils.showToastLong(str("revanced_custom_filter_toast_reset"));
            groups = Objects.requireNonNull(CustomFilterGroup.parseCustomFilterGroups());
        }

        if (!groups.isEmpty()) {
            CustomFilterGroup[] groupsArray = groups.toArray(new CustomFilterGroup[0]);
            Logger.printDebug(()-> "Using Custom filters: " + Arrays.toString(groupsArray));
            addPathCallbacks(groupsArray);
        }
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // All callbacks are custom filter groups.
        CustomFilterGroup custom = (CustomFilterGroup) matchedGroup;
        if (custom.startsWith && contentIndex != 0) {
            return false;
        }
        if (custom.bufferSearch != null && !custom.bufferSearch.matches(protobufBufferArray)) {
            return false;
        }
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}