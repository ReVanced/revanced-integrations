package app.revanced.integrations.patches.components;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;

import app.revanced.integrations.patches.ReturnYouTubeDislikePatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ReturnYouTubeDislikeFilterPatch extends Filter {

    private static final String VIDEO_ID_PREFIX_TEXT = "ic_right_dislike_off_shadowed";

    private final ByteArrayAsStringFilterGroup videoIdFilterGroup
            = new ByteArrayAsStringFilterGroup(null, VIDEO_ID_PREFIX_TEXT);

    public ReturnYouTubeDislikeFilterPatch() {
        pathFilterGroupList.addAll(
                new StringFilterGroup(SettingsEnum.RYD_SHORTS, "|shorts_dislike_button.eml|")
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        FilterGroup.FilterGroupResult result = videoIdFilterGroup.check(protobufBufferArray);
        if (result.isFiltered()) {
            final int minimumYouTubeVideoIdLength = 11;
            final int subStringSearchStartIndex = result.getMatchedIndex() + VIDEO_ID_PREFIX_TEXT.length();
            String videoId = findSubString(protobufBufferArray, subStringSearchStartIndex,
                    minimumYouTubeVideoIdLength, (byte) ':');
            if (videoId != null) {
                LogHelper.printDebug(() -> "Found shorts litho video id: " + videoId);
                ReturnYouTubeDislikePatch.newVideoLoaded(videoId, false);
            }
        }

        return false;
    }

    /**
     * Find a minimum length ASCII substring starting from a given index,
     * and the substring ends with a specific character.
     *
     * Similar to the String finding code in {@link LithoFilterPatch},
     * but refactoring it to also handle this use case became messy and overly complicated.
     *
     * @param terminatingByte Terminating byte at the end of a the minimum ascii substring.
     *                        The substring will include this terminating character,
     *                        if it appears before the minimum length.
     */
    @Nullable
    private static String findSubString(byte[] buffer, int bufferStartIndex,
                                        int minimumSubStringLength, byte terminatingByte) {
        // Valid ASCII values (ignore control characters).
        final int minimumAscii = 32;  // 32 = space character
        final int maximumAscii = 126; // 127 = delete character

        final int bufferLength = buffer.length;
        int start = bufferStartIndex;
        int end = 0;
        while (end < bufferLength) {
            final int value = buffer[end];
            if (value == terminatingByte) {
                final int subStringLength = end - start;
                if (subStringLength >= minimumSubStringLength) {
                    return new String(buffer, start, subStringLength, StandardCharsets.US_ASCII);
                }
            }
            end++;
            if (value < minimumAscii || value > maximumAscii) {
                start = end;
            }
        }
        return null;
    }
}
