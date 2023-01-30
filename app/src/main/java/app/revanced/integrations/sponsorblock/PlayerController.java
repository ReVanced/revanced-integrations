package app.revanced.integrations.sponsorblock;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Class is not thread safe. All methods must be called on the main thread unless otherwise specified.
 */
public class PlayerController {
    @Nullable
    private static String currentVideoId;
    @Nullable
    private static SponsorSegment[] sponsorSegmentsOfCurrentVideo;
    /**
     * current segment that user can manually skip
     */
    @Nullable
    private static SponsorSegment segmentCurrentlyPlayingToManuallySkip;
    /**
     * Next segment that is scheduled to autoskip
     */
    @Nullable
    private static SponsorSegment nextSegmentToAutoSkip;
    private static String timeWithoutSegments = "";
    private static long allowNextSkipRequestTime = 0L;
    private static long lastKnownVideoTime = -1L;
    private static boolean settingsInitialized;
    private static float sponsorBarLeft = 1f;
    private static float sponsorBarRight = 1f;
    private static float sponsorBarThickness = 2f;

    @Nullable
    static SponsorSegment[] getSponsorSegmentsOfCurrentVideo() {
        return sponsorSegmentsOfCurrentVideo;
    }

    static void setSponsorSegmentsOfCurrentVideo(@NonNull SponsorSegment[] segments) {
        Arrays.sort(segments);
        sponsorSegmentsOfCurrentVideo = segments;
        calculateTimeWithoutSegments();
    }

    @Nullable
    static String getCurrentVideoId() {
        return currentVideoId;
    }

    /**
     * Clears all downloaded data
     */
    private static void clearDownloadedData() {
        currentVideoId = null;
        sponsorSegmentsOfCurrentVideo = null;
        timeWithoutSegments = "";
        nextSegmentToAutoSkip = null; // prevent any existing scheduled skip from running
    }

    /**
     * Injection point.
     * Called when creating some kind of youtube internal player, every time when new video starts to play
     */
    public static void initialize(Object _o) {
        try {
            ReVancedUtils.verifyOnMainThread();
            lastKnownVideoTime = 0;
            SkipSegmentView.hide();
            NewSegmentHelperLayout.hide();
            LogHelper.printDebug(() -> "initialized");
        } catch (Exception ex) {
            LogHelper.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Patch injection point
     */
    public static void setCurrentVideoId(@Nullable String videoId) {
        try {
            if (videoId == null || !SettingsEnum.SB_ENABLED.getBoolean()) {
                clearDownloadedData();
                return;
            }
            if (PlayerType.getCurrent() == PlayerType.NONE) {
                LogHelper.printDebug(() -> "ignoring shorts video");
                clearDownloadedData();
                return;
            }
            if (videoId.equals(currentVideoId)) {
                return;
            }

            if (!settingsInitialized) {
                SponsorBlockSettings.update();
                settingsInitialized = true;
            }

            clearDownloadedData();
            currentVideoId = videoId;

            // if opening new video in same player, then skip button may be showing from prior video
            SkipSegmentView.hide();

            LogHelper.printDebug(() -> "setCurrentVideoId: " + videoId);

            String videoIdToDownload = videoId; // make a copy, to use off main thread
            ReVancedUtils.runOnBackgroundThread(() -> {
                try {
                    executeDownloadSegments(videoIdToDownload);
                } catch (Exception e) {
                    LogHelper.printException(() -> "Failed to download segments", e);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "setCurrentVideoId failure", ex);
        }
    }

    /**
     * Must be called off main thread
     */
    static void executeDownloadSegments(String videoId) {
        Objects.requireNonNull(videoId);
        try {
            SponsorSegment[] segments = SBRequester.getSegments(videoId);

            LogHelper.printDebug(() -> {
                StringBuilder builder = new StringBuilder("Downloaded segments:");
                for (SponsorSegment segment : segments) {
                    builder.append('\n').append(segment);
                }
                return builder.toString();
            });

            ReVancedUtils.runOnMainThread(()-> {
                if (!videoId.equals(currentVideoId)) {
                    // user changed videos before get segments network call could complete
                    LogHelper.printDebug(() -> "ignoring stale segments for prior video: " + videoId);
                    return;
                }
                setSponsorSegmentsOfCurrentVideo(segments);
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "executeDownloadSegments failure", ex);
        }
    }

    /**
     * Injection point.  This appears to be called once every 100ms
     */
    public static void setVideoTime(long millis) {
        try {
            if (!SettingsEnum.SB_ENABLED.getBoolean()) return;

            lastKnownVideoTime = millis;

            if (sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) return;

            LogHelper.printDebug(() -> "setCurrentVideoTime: current video time: " + millis);

            if (isAtEndOfVideo()) {
                ShieldButton.hide();
                VotingButton.hide();
            }

            // to debug the segmentToSkip stale detection, set this to a very large value (12,000 or more)
            // then manually seek to a different location just before an autoskip segment starts
            final long START_TIMER_BEFORE_SEGMENT_MILLIS = 1200; // must be larger than the average time between calls to this method
            final long startTimerAtMillis = millis + START_TIMER_BEFORE_SEGMENT_MILLIS;

            segmentCurrentlyPlayingToManuallySkip = null;

            for (final SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
                if (millis < segment.start) { // segment is upcoming
                    if (startTimerAtMillis < segment.start)
                        break; // not inside any segments, and no upcoming segments are close enough to schedule a task

                    if (!segment.category.behaviour.skip)
                        break;

                    if (nextSegmentToAutoSkip != segment) {
                        LogHelper.printDebug(() -> "scheduling segmentToSkip");
                        ReVancedUtils.runOnMainThreadDelayed(() -> {
                            if (nextSegmentToAutoSkip != segment) {
                                LogHelper.printDebug(() -> "ignoring stale autoskip: " + segment);
                            } else {
                                // clear the field, so if user rewinds then timer can again be set for the same segment
                                nextSegmentToAutoSkip = null;
                                skipSegment(segment, false);
                            }
                        }, segment.start - millis);
                    }
                    break;
                }

                if (segment.end < millis)
                    continue; // already past this segment

                // we are in the segment!
                if (segment.category.behaviour.skip && !(segment.category.behaviour.key.equals("skip-once") && segment.didAutoSkipped)) {
                    skipSegment(segment, false);
                    break;
                } else {
                    segmentCurrentlyPlayingToManuallySkip = segment;
                    continue; // keep looking, as there may be an autoskip segment coming up very soon
                }
            }
            if (segmentCurrentlyPlayingToManuallySkip != null) {
                SkipSegmentView.show();
            } else {
                SkipSegmentView.hide();
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "setVideoTime failure", e);
        }
    }

    public static long getCurrentVideoLength() {
        return VideoInformation.getCurrentVideoLength();
    }

    public static long getLastKnownVideoTime() {
        return lastKnownVideoTime;
    }

    public static boolean isAtEndOfVideo() {
        return getLastKnownVideoTime() >= getCurrentVideoLength();
    }

    /**
     * Injection point
     */
    public static void setSponsorBarAbsoluteLeft(final Rect rect) {
        setSponsorBarAbsoluteLeft(rect.left);
    }

    public static void setSponsorBarAbsoluteLeft(final float left) {
        if (sponsorBarLeft != left) {
            LogHelper.printDebug(() -> String.format("setSponsorBarLeft: left=%.2f", left));
            sponsorBarLeft = left;
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarRect(final Object self) {
        try {
            Field field = self.getClass().getDeclaredField("replaceMeWithsetSponsorBarRect");
            field.setAccessible(true);
            Rect rect = (Rect) field.get(self);
            if (rect != null) {
                setSponsorBarAbsoluteLeft(rect.left);
                setSponsorBarAbsoluteRight(rect.right);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "setSponsorBarRect failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarAbsoluteRight(final Rect rect) {
        setSponsorBarAbsoluteRight(rect.right);
    }

    public static void setSponsorBarAbsoluteRight(final float right) {
        if (sponsorBarRight != right) {
            LogHelper.printDebug(() -> String.format("setSponsorBarRight: right=%.2f", right));
            sponsorBarRight = right;
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarThickness(final int thickness) {
        try {
            setSponsorBarThickness((float) thickness);
        } catch (Exception ex) {
            LogHelper.printException(() -> "setSponsorBarThickness failure", ex);
        }
    }

    public static void setSponsorBarThickness(final float thickness) {
        if (sponsorBarThickness != thickness) {
            LogHelper.printDebug(() -> String.format("setSponsorBarThickness: thickness=%.2f", thickness));
            sponsorBarThickness = thickness;
        }
    }

    public static void onSkipSponsorClicked() {
        if (segmentCurrentlyPlayingToManuallySkip != null) {
            skipSegment(segmentCurrentlyPlayingToManuallySkip, true);
        } else {
            LogHelper.printException(() -> "error: segment not available to skip"); // should never happen
        }
    }

    /**
     * Injection point
     */
    public static void addSkipSponsorView15(final View view) {
        try {
            LogHelper.printDebug(() -> "addSkipSponsorView15: view=" + view);

            ReVancedUtils.runOnMainThreadDelayed(() -> {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(2);
                NewSegmentHelperLayout.context = viewGroup.getContext();
            }, 500);
        } catch (Exception ex) {
            LogHelper.printException(() -> "addSkipSponsorView15 failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static String appendTimeWithoutSegments(String totalTime) {
        try {
            if (SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean()
                    && !TextUtils.isEmpty(totalTime) && getCurrentVideoLength() > 1) {
                return totalTime + timeWithoutSegments;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "appendTimeWithoutSegments failure", ex);
        }

        return totalTime;
    }

    private static void calculateTimeWithoutSegments() {
        final long currentVideoLength = getCurrentVideoLength();
        if (!SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean() || currentVideoLength <= 1
                || sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) {
            timeWithoutSegments = "";
            return;
        }

        long timeWithoutSegmentsValue = currentVideoLength + 500; // YouTube:tm:
        for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
            timeWithoutSegmentsValue -= segment.end - segment.start;
        }
        final long hours = timeWithoutSegmentsValue / 3600000;
        final long minutes = (timeWithoutSegmentsValue / 60000) % 60;
        final long seconds = (timeWithoutSegmentsValue / 1000) % 60;
        String format = (hours > 0 ? "%d:%02" : "%") + "d:%02d"; // mmLul
        String formatted = hours > 0 ? String.format(format, hours, minutes, seconds) : String.format(format, minutes, seconds);

        timeWithoutSegments = String.format(" (%s)", formatted);
    }

    /**
     * Injection point
     */
    public static void drawSponsorTimeBars(final Canvas canvas, final float posY) {
        try {
            if (sponsorBarThickness < 0.1) return;
            if (sponsorSegmentsOfCurrentVideo == null) return;

            final float thicknessDiv2 = sponsorBarThickness / 2;
            final float top = posY - thicknessDiv2;
            final float bottom = posY + thicknessDiv2;
            final float absoluteLeft = sponsorBarLeft;
            final float absoluteRight = sponsorBarRight;

            final float tmp1 = 1f / (float) VideoInformation.getCurrentVideoLength() * (absoluteRight - absoluteLeft);
            for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
                float left = segment.start * tmp1 + absoluteLeft;
                float right = segment.end * tmp1 + absoluteLeft;
                canvas.drawRect(left, top, right, bottom, segment.category.paint);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "drawSponsorTimeBars failure", ex);
        }
    }

    public static void skipRelativeMilliseconds(int millisRelative) {
        skipToMillisecond(lastKnownVideoTime + millisRelative);
    }

    public static boolean skipToMillisecond(long millisecond) {
        // in 15.x if sponsor clip hits the end, then it crashes the app, because of too many function invocations
        // I put this block so that skip can be made only once per some time
        try {
            final long now = System.currentTimeMillis();
            if (now < allowNextSkipRequestTime) {
                LogHelper.printDebug(() -> "skipToMillisecond: to fast, slow down, because you'll fail");
                return false;
            }
            allowNextSkipRequestTime = now + 100;

            LogHelper.printDebug(() -> "Skipping to millis=" + millisecond);
            lastKnownVideoTime = millisecond;
            VideoInformation.seekTo(millisecond);
        } catch (Exception e) {
            LogHelper.printException(() -> "Cannot skip to millisecond", e);
        }

        return true;
    }

    private static void skipSegment(SponsorSegment segment, boolean userManuallySkipped) {
        try {
            LogHelper.printDebug(() -> "Skipping segment: " + segment);

            boolean didSucceed = skipToMillisecond(segment.end + 2);
            if (didSucceed && !userManuallySkipped) {
                segment.didAutoSkipped = true;
            }
            if (SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean() && !userManuallySkipped)
                SkipSegmentView.notifySkipped(segment);
            SkipSegmentView.hide();

            if (segment.category == SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
                // skipped segment was a preview of unsubmitted segment
                // remove the segment from the UI view
                SponsorSegment[] newSegments = new SponsorSegment[sponsorSegmentsOfCurrentVideo.length - 1];
                int i = 0;
                for (SponsorSegment sponsorSegment : sponsorSegmentsOfCurrentVideo) {
                    if (sponsorSegment != segment)
                        newSegments[i++] = sponsorSegment;
                }
                setSponsorSegmentsOfCurrentVideo(newSegments);
            } else {
                SponsorBlockUtils.sendViewRequestAsync(lastKnownVideoTime, segment);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }
}
