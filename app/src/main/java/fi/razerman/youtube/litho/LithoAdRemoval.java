package fi.razerman.youtube.litho;

import android.util.Log;
import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import fi.razerman.youtube.Helpers.SharedPrefs;
import fi.razerman.youtube.XGlobals;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* loaded from: classes6.dex */
public class LithoAdRemoval {
    private static final byte[] endRelatedPageAd = {112, 97, 103, 101, 97, 100};
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static boolean isExperimentalAdRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_ad_removal", true);
    }

    public static boolean isExperimentalMerchandiseRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_merchandise", false);
    }

    public static boolean isExperimentalCommunityPostRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_community_posts", false);
    }

    public static boolean isExperimentalMovieUpsellRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_movie_upsell", false);
    }

    public static boolean isExperimentalCompactBannerRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_compact_banner", false);
    }

    public static boolean isExperimentalCommentsRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_comments", false);
    }

    public static boolean isExperimentalCompactMovieRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_compact_movie", false);
    }

    public static boolean isExperimentalHorizontalMovieShelfRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_horizontal_movie_shelf", false);
    }

    public static boolean isInFeedSurvey() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_in_feed_survey", false);
    }

    public static boolean isShortsShelf() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_shorts_shelf", false);
    }

    public static boolean isCommunityGuidelines() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_community_guidelines", false);
    }

    public static boolean containsAd(String value) {
        if (!(isExperimentalAdRemoval() || isExperimentalMerchandiseRemoval() || isExperimentalCommunityPostRemoval() || isExperimentalMovieUpsellRemoval() || isExperimentalCompactBannerRemoval() || isExperimentalCommentsRemoval() || isExperimentalCompactMovieRemoval() || isExperimentalHorizontalMovieShelfRemoval() || isInFeedSurvey() || isShortsShelf() || isCommunityGuidelines()) || value == null || value.isEmpty()) {
            return false;
        }
        List<String> blockList = new ArrayList<>();
        if (isExperimentalAdRemoval()) {
            blockList.add("_ad");
            blockList.add("ad_badge");
        }
        if (isExperimentalMerchandiseRemoval()) {
            blockList.add("product_carousel");
        }
        if (isExperimentalCommunityPostRemoval()) {
            blockList.add("post_base_wrapper");
        }
        if (isExperimentalMovieUpsellRemoval()) {
            blockList.add("movie_and_show_upsell_card");
        }
        if (isExperimentalCompactBannerRemoval()) {
            blockList.add("compact_banner");
        }
        if (isExperimentalCommentsRemoval()) {
            blockList.add("comments_composite_entry_point");
        }
        if (isExperimentalCompactMovieRemoval()) {
            blockList.add("compact_movie");
        }
        if (isExperimentalHorizontalMovieShelfRemoval()) {
            blockList.add("horizontal_movie_shelf");
        }
        if (isInFeedSurvey()) {
            blockList.add("in_feed_survey");
        }
        if (isShortsShelf()) {
            blockList.add("shorts_shelf");
        }
        if (isCommunityGuidelines()) {
            blockList.add("community_guidelines");
        }
        for (String s : blockList) {
            if (value.contains(s)) {
                if (XGlobals.debug) {
                    Log.d("TemplateBlocked", value);
                }
                return true;
            }
        }
        if (!XGlobals.debug) {
            return false;
        }
        Log.d("Template", value);
        return false;
    }

    public static boolean containsAd(String value, ByteBuffer buffer) {
        try {
            if (!(isExperimentalAdRemoval() || isExperimentalMerchandiseRemoval() || isExperimentalCommunityPostRemoval() || isExperimentalMovieUpsellRemoval() || isExperimentalCompactBannerRemoval() || isExperimentalCommentsRemoval() || isExperimentalCompactMovieRemoval() || isExperimentalHorizontalMovieShelfRemoval() || isInFeedSurvey() || isShortsShelf() || isCommunityGuidelines()) || value == null || value.isEmpty()) {
                return false;
            }
            List<String> blockList = new ArrayList<>();
            if (isExperimentalAdRemoval()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
            }
            if (isExperimentalMerchandiseRemoval()) {
                blockList.add("product_carousel");
            }
            if (isExperimentalCommunityPostRemoval()) {
                blockList.add("post_base_wrapper");
            }
            if (isExperimentalMovieUpsellRemoval()) {
                blockList.add("movie_and_show_upsell_card");
            }
            if (isExperimentalCompactBannerRemoval()) {
                blockList.add("compact_banner");
            }
            if (isExperimentalCommentsRemoval()) {
                blockList.add("comments_composite_entry_point");
            }
            if (isExperimentalCompactMovieRemoval()) {
                blockList.add("compact_movie");
            }
            if (isExperimentalHorizontalMovieShelfRemoval()) {
                blockList.add("horizontal_movie_shelf");
            }
            if (isInFeedSurvey()) {
                blockList.add("in_feed_survey");
            }
            if (isShortsShelf()) {
                blockList.add("shorts_shelf");
            }
            if (isCommunityGuidelines()) {
                blockList.add("community_guidelines");
            }
            if (!value.contains("related_video_with_context") || indexOf(buffer.array(), endRelatedPageAd) <= 0) {
                for (String s : blockList) {
                    if (value.contains(s)) {
                        if (XGlobals.debug) {
                            Log.d("TemplateBlocked", value);
                        }
                        return true;
                    }
                }
                if (!XGlobals.debug) {
                    return false;
                }
                if (value.contains("related_video_with_context")) {
                    Log.d("Template", value + " | " + bytesToHex(buffer.array()));
                    return false;
                }
                Log.d("Template", value);
                return false;
            }
            if (XGlobals.debug) {
                Log.d("TemplateBlocked", value);
            }
            return true;
        } catch (Exception ex) {
            Log.e("Template", ex.getMessage(), ex);
            return false;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:13:0x0019, code lost:
        r0 = r0 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static int indexOf(byte[] r4, byte[] r5) {
        /*
            int r2 = r5.length
            if (r2 != 0) goto L_0x0005
            r0 = 0
        L_0x0004:
            return r0
        L_0x0005:
            r0 = 0
        L_0x0006:
            int r2 = r4.length
            int r3 = r5.length
            int r2 = r2 - r3
            int r2 = r2 + 1
            if (r0 >= r2) goto L_0x001f
            r1 = 0
        L_0x000e:
            int r2 = r5.length
            if (r1 >= r2) goto L_0x0004
            int r2 = r0 + r1
            byte r2 = r4[r2]
            byte r3 = r5[r1]
            if (r2 == r3) goto L_0x001c
            int r0 = r0 + 1
            goto L_0x0006
        L_0x001c:
            int r1 = r1 + 1
            goto L_0x000e
        L_0x001f:
            r0 = -1
            goto L_0x0004
        */
        throw new UnsupportedOperationException("Method not decompiled: fi.razerman.youtube.litho.LithoAdRemoval.indexOf(byte[], byte[]):int");
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }
}
