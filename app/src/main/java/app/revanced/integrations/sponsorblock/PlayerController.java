package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.StringRef.str;

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
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
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
     * Initializes SponsorBlock when the video player starts playing a new video.
     */
    public static void initialize(Object _o) {
        try {
            ReVancedUtils.verifyOnMainThread();
            SponsorBlockView.hideSkipButton();
            NewSegmentHelperLayout.hide();
            LogHelper.printDebug(() -> "Initialized SponsorBlock");
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to initialize SponsorBlock", ex);
        }
    }

    /**
     * Injection point.
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
            SponsorBlockView.hideSkipButton();

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
                    LogHelper.printDebug(() -> "Ignoring stale segments for prior video: " + videoId);
                    return;
                }
                setSponsorSegmentsOfCurrentVideo(segments);
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "executeDownloadSegments failure", ex);
        }
    }

    /**
     * Injection point.
     * Updates SponsorBlock every 1000ms.
     */
    public static void setVideoTime(long millis) {
        try {
            if (!SettingsEnum.SB_ENABLED.getBoolean()) return;

            if (sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) return;

            LogHelper.printDebug(() -> "setVideoTime: " + millis);

            if (VideoInformation.isAtEndOfVideo()) {
                ShieldButton.hide();
                VotingButton.hide();
            }

            // to debug the segmentToSkip stale detection, set this to a very large value (12,000 or more)
            // then manually seek to a different location just before an autoskip segment starts
            final long START_TIMER_BEFORE_SEGMENT_MILLIS = 2000; // must be larger than the average time between calls to this method
            final long startTimerAtMillis = millis + START_TIMER_BEFORE_SEGMENT_MILLIS;

            segmentCurrentlyPlayingToManuallySkip = null;
            boolean foundUpcomingAutoSkipSegment = false;

            for (final SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
                if (millis < segment.start) { // segment is upcoming
                    if (startTimerAtMillis < segment.start)
                        break; // no upcoming segments are close enough to schedule a task

                    if (!segment.shouldAutoSkip())
                        break; // not an autoskip segment, or it's a skip once and has already gone

                    foundUpcomingAutoSkipSegment = true;
                    if (nextSegmentToAutoSkip != segment) {
                        LogHelper.printDebug(() -> "Scheduling skipping segment automatically");
                        nextSegmentToAutoSkip = segment;
                        ReVancedUtils.runOnMainThreadDelayed(() -> {
                            if (nextSegmentToAutoSkip != segment) {
                                LogHelper.printDebug(() -> "Ignoring stale scheduled skip: " + segment);
                            } else {
                                LogHelper.printDebug(() -> "Running scheduled skip");
                                nextSegmentToAutoSkip = null;
                                skipSegment(segment, false);
                            }
                        }, segment.start - millis);
                    }
                    break;
                }

                if (segment.end <= millis)
                    continue; // already past this segment

                // we are in the segment!
                if (segment.shouldAutoSkip()) {
                    skipSegment(segment, false);
                    break;
                } else {
                    segmentCurrentlyPlayingToManuallySkip = segment;
                    // keep looking. there may be an upcoming autoskip,
                    // or there may be another smaller segment nested inside this segment
                    continue;
                }
            }
            if (!foundUpcomingAutoSkipSegment && nextSegmentToAutoSkip != null) {
                LogHelper.printDebug(() -> "Clearing scheduled autoskip");
                nextSegmentToAutoSkip = null;
            }
            if (segmentCurrentlyPlayingToManuallySkip != null) {
                SponsorBlockView.showSkipButton();
            } else {
                SponsorBlockView.hideSkipButton();
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "setVideoTime failure", e);
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarAbsoluteLeft(final Rect rect) {
        setSponsorBarAbsoluteLeft(rect.left);
    }

    public static void setSponsorBarAbsoluteLeft(final float left) {
        if (sponsorBarLeft != left) {
            LogHelper.printDebug(() -> String.format("setSponsorBarAbsoluteLeft: left=%.2f", left));
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
            LogHelper.printDebug(() -> String.format("setSponsorBarAbsoluteRight: right=%.2f", right));
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
            LogHelper.printDebug(() -> String.format("setSponsorBarThickness: %.2f", thickness));
            sponsorBarThickness = thickness;
        }
    }

    public static void onSkipSponsorClicked() {
        if (segmentCurrentlyPlayingToManuallySkip != null) {
            skipSegment(segmentCurrentlyPlayingToManuallySkip, true);
        } else {
            SponsorBlockView.hideSkipButton();
            LogHelper.printException(() -> "error: segment not available to skip"); // should never happen
        }
    }

    /**
     * Injection point
     */
    public static void addSkipSponsorView15(final View view) {
        try {
            LogHelper.printDebug(() -> "addSkipSponsorView15: " + view);

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
                    && !TextUtils.isEmpty(totalTime) && VideoInformation.getCurrentVideoLength() > 1) {
                return totalTime + timeWithoutSegments;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "appendTimeWithoutSegments failure", ex);
        }

        return totalTime;
    }

    private static void calculateTimeWithoutSegments() {
        final long currentVideoLength = VideoInformation.getCurrentVideoLength();
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

    private static void skipSegment(SponsorSegment segment, boolean userManuallySkipped) {
        try {
            LogHelper.printDebug(() -> "Skipping segment: " + segment);

            SponsorBlockView.hideSkipButton();
            VideoInformation.seekTo(segment.end);

            if (!userManuallySkipped) {
                segment.didAutoSkipped = true;
                if (SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean()) {
                    showSkippedSegmentToast(segment);
                }
            }

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
                SponsorBlockUtils.sendViewRequestAsync(VideoInformation.getVideoTime(), segment);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }


    private static int numberOfSegmentsSkipped;
    private static String segmentSkipMessage;

    private static void showSkippedSegmentToast(SponsorSegment segment) {
        ReVancedUtils.verifyOnMainThread();
        numberOfSegmentsSkipped++;
        if (numberOfSegmentsSkipped > 1) {
            return; // toast already scheduled
        }
        segmentSkipMessage = segment.category.skipMessage.toString();

        final long delayToToastMilliseconds = 200; // also the maximum time between skips to be considered skipping multiple segments
        ReVancedUtils.runOnMainThreadDelayed(() -> {
            try {
                if (segmentSkipMessage == null) {
                    LogHelper.printException(() -> "No skip message to display"); // should never happen
                    return;
                }
                ReVancedUtils.showToastShort(numberOfSegmentsSkipped == 1
                        ? segmentSkipMessage
                        : str("skipped_multiple_segments"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "showSkippedSegmentToast failure", ex);
            } finally {
                numberOfSegmentsSkipped = 0;
                segmentSkipMessage = null;
            }
        }, delayToToastMilliseconds);
    }

}
