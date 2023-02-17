package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.patches.playback.speed.RememberPlaybackRatePatch;
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
    private static SponsorSegment[] segmentsOfCurrentVideo;

    /**
     * Current segment that user can manually skip
     */
    @Nullable
    private static SponsorSegment segmentCurrentlyPlaying;
    /**
     * Currently playing manual skip segment, that is scheduled to hide.
     * This will always be NULL or equal to {@link #segmentCurrentlyPlaying}
     */
    @Nullable
    private static SponsorSegment scheduledHideSegment;
    /**
     * Upcoming segment that is scheduled to either autoskip or show the manual skip button
     */
    @Nullable
    private static SponsorSegment scheduledUpcomingSegment;

    private static String timeWithoutSegments = "";
    private static boolean settingsInitialized;

    private static float sponsorBarLeft = 1f;
    private static float sponsorBarRight = 1f;
    private static float sponsorBarThickness = 2f;

    @Nullable
    static SponsorSegment[] getSegmentsOfCurrentVideo() {
        return segmentsOfCurrentVideo;
    }

    static void setSegmentsOfCurrentVideo(@NonNull SponsorSegment[] segments) {
        Arrays.sort(segments);
        segmentsOfCurrentVideo = segments;
        calculateTimeWithoutSegments();
    }

    public static boolean currentVideoHasSegments() {
        return segmentsOfCurrentVideo != null && segmentsOfCurrentVideo.length > 0;
    }

    @Nullable
    static String getCurrentVideoId() {
        return currentVideoId;
    }

    /**
     * Clears all downloaded data
     */
    private static void clearData() {
        currentVideoId = null;
        segmentsOfCurrentVideo = null;
        timeWithoutSegments = "";
        segmentCurrentlyPlaying = null;
        scheduledUpcomingSegment = null; // prevent any existing scheduled skip from running
        scheduledHideSegment = null;
        toastSegmentSkipped = null; // prevent any scheduled skip toasts from showing
        toastNumberOfSegmentsSkipped = 0;
    }

    /**
     * Injection point.
     * Initializes SponsorBlock when the video player starts playing a new video.
     */
    public static void initialize(Object _o) {
        try {
            ReVancedUtils.verifyOnMainThread();
            clearData();
            SponsorBlockView.hideSkipButton();
            SponsorBlockView.hideNewSegmentLayout();
            SponsorBlockUtils.clearUnsubmittedSegmentTimes();
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
                clearData();
                return;
            }
            if (videoId.equals(currentVideoId)) {
                return;
            }
            if (PlayerType.getCurrent().isNoneOrHidden()) {
                LogHelper.printDebug(() -> "ignoring short or story");
                clearData();
                return;
            }
            if (!ReVancedUtils.isNetworkConnected()) {
                LogHelper.printDebug(() -> "Network not connected, ignoring video");
                clearData();
                return;
            }

            if (!settingsInitialized) {
                SponsorBlockSettings.update();
                settingsInitialized = true;
            }

            clearData();
            currentVideoId = videoId;

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

            ReVancedUtils.runOnMainThread(()-> {
                if (!videoId.equals(currentVideoId)) {
                    // user changed videos before get segments network call could complete
                    LogHelper.printDebug(() -> "Ignoring segments for prior video: " + videoId);
                    return;
                }
                setSegmentsOfCurrentVideo(segments);
                setVideoTime(VideoInformation.getVideoTime()); // check for any skips now, instead of waiting for the next update
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "executeDownloadSegments failure", ex);
        }
    }

    /**
     * Injection point.
     * Updates SponsorBlock every 1000ms.
     * When changing videos, this is first called with value 0 and then the video is changed.
     */
    public static void setVideoTime(long millis) {
        try {
            if (!SettingsEnum.SB_ENABLED.getBoolean()
                || PlayerType.getCurrent().isNoneOrHidden() // shorts playback
                || segmentsOfCurrentVideo == null || segmentsOfCurrentVideo.length == 0) {
                return;
            }
            if (VideoInformation.getCurrentVideoLength() <= 0) {
                LogHelper.printDebug(() -> "Video is not yet loaded (video has no length)."
                        + "  Ignoring setVideoTime call of time: " + millis);
                return;
            }
            LogHelper.printDebug(() -> "setVideoTime: " + millis);

            // to debug the timing logic, set this to a very large value (5000 or more)
            // then try manually seeking just playback reaches a skip/hide of different segments
            final long lookAheadMilliseconds = 1500; // must be larger than the average time between calls to this method
            final float playbackRate = RememberPlaybackRatePatch.getCurrentPlaybackRate();
            final long startTimerLookAheadThreshold = millis + (long)(playbackRate * lookAheadMilliseconds);

            SponsorSegment foundCurrentSegment = null;
            SponsorSegment foundUpcomingSegment = null;

            for (final SponsorSegment segment : segmentsOfCurrentVideo) {
                if (segment.category.behaviour == SponsorBlockSettings.SegmentBehaviour.IGNORE) {
                    continue; // while video is opened, user changed a category behavior to ignore
                }
                if (segment.end <= millis) {
                    continue; // past this segment
                }

                if (segment.start <= millis) {
                    // we are in the segment!
                    if (segment.shouldAutoSkip()) {
                        skipSegment(segment, millis, false);
                        return; // must return, as skipping causes a recursive call back into this method
                    }

                    // first found segment, or it's an embedded segment and fully inside the outer segment
                    if (foundCurrentSegment == null || foundCurrentSegment.containsSegment(segment)) {
                        // If the found segment is not currently displayed, then do not show if the segment is nearly over.
                        // This check prevents the skip button text from rapidly changing when multiple segments end at nearly the same time.
                        // Also prevents showing the skip button if user seeks into the last half second of the segment.
                        final long minMillisOfSegmentRemainingThreshold = 500;
                        if (segmentCurrentlyPlaying == segment
                                || !segment.timeIsNearEnd(millis, minMillisOfSegmentRemainingThreshold)) {
                            foundCurrentSegment = segment;
                        } else {
                            LogHelper.printDebug(() -> "Ignoring segment that ends very soon: " + segment);
                        }
                    }
                    // Keep iterating and looking. There may be an upcoming autoskip,
                    // or there may be another smaller segment nested inside this segment
                    continue;
                }

                // segment is upcoming
                if (startTimerLookAheadThreshold < segment.start) {
                    break; // segment is not close enough to schedule, and no segments after this are of interest
                }
                if (segment.shouldAutoSkip()) { // upcoming autoskip
                    foundUpcomingSegment = segment;
                    break; // must stop here
                }

                // upcoming manual skip

                // it's the first upcoming manual skip found, or the segment is fully contained inside the prior found upcoming segment
                if (foundUpcomingSegment == null || foundUpcomingSegment.containsSegment(segment)) {
                    // Only schedule, if the segment start time is not near the end time of the current segment.
                    // This check is needed to prevent scheduled hide and show from clashing with each other.
                    final long minTimeBetweenStartEndOfSegments = 1000;
                    if (foundCurrentSegment == null
                            || !foundCurrentSegment.timeIsNearEnd(segment.start, minTimeBetweenStartEndOfSegments)) {
                        foundUpcomingSegment = segment;
                    } else {
                        LogHelper.printDebug(() -> "Not scheduling segment (start time is near end of current segment): " + segment);
                    }
                }
            }


            if (segmentCurrentlyPlaying != foundCurrentSegment) {
                if (foundCurrentSegment == null) {
                    LogHelper.printDebug(() -> "Hiding segment: " + segmentCurrentlyPlaying);
                    segmentCurrentlyPlaying = null;
                    SponsorBlockView.hideSkipButton();
                }  else {
                    segmentCurrentlyPlaying = foundCurrentSegment;
                    LogHelper.printDebug(() -> "Showing segment: " + segmentCurrentlyPlaying);
                    SponsorBlockView.showSkipButton(foundCurrentSegment.category);
                }
            }

            // must be greater than the average time between updates to VideoInformation time
            final long videoInformationTimeUpdateThresholdMilliseconds = 250;

            if (scheduledHideSegment != foundCurrentSegment) {
                if (foundCurrentSegment == null || !foundCurrentSegment.timeIsNearEnd(millis, lookAheadMilliseconds)) {
                    // nothing to schedule, or segment is too far away to schedule a hide
                    if (scheduledHideSegment != null) {
                        LogHelper.printDebug(() -> "Clearing scheduled hide: " + scheduledHideSegment);
                        scheduledHideSegment = null;
                    }
                } else {
                    scheduledHideSegment = foundCurrentSegment;
                    final SponsorSegment segmentToHide = foundCurrentSegment;

                    LogHelper.printDebug(() -> "Scheduling hide segment: " + segmentToHide + " playbackRate: " + playbackRate);
                    final long delayUntilHide = (long) ((segmentToHide.end - millis) / playbackRate);
                    ReVancedUtils.runOnMainThreadDelayed(() -> {
                        if (scheduledHideSegment != segmentToHide) {
                            LogHelper.printDebug(() -> "Ignoring old scheduled hide segment: " + segmentToHide);
                            return;
                        }
                        scheduledHideSegment = null;

                        if (!segmentToHide.timeIsNearEnd(VideoInformation.getVideoTime(), videoInformationTimeUpdateThresholdMilliseconds)) {
                            // current video time is not what's expected.  User paused playback
                            LogHelper.printDebug(() -> "Ignoring outdated scheduled hide: " + segmentToHide);
                            return;
                        }
                        LogHelper.printDebug(() -> "Running scheduled hide of segment: " + segmentToHide);
                        // Need more than just hide the skip button, as this may have been an embedded segment
                        // Instead call back into setVideoTime to check everything again.
                        // Should not use VideoInformation time as it is less accurate,
                        // but this scheduled handler was scheduled precisely so we can just use the segment end time
                        segmentCurrentlyPlaying = null;
                        SponsorBlockView.hideSkipButton();
                        setVideoTime(segmentToHide.end);
                    }, delayUntilHide);
                }
            }

            if (scheduledUpcomingSegment != foundUpcomingSegment) {
                if (foundUpcomingSegment == null) {
                    LogHelper.printDebug(() -> "Clearing scheduled segment: " + scheduledUpcomingSegment);
                    scheduledUpcomingSegment = null;
                } else {
                    scheduledUpcomingSegment = foundUpcomingSegment;
                    final SponsorSegment segmentToSkip = foundUpcomingSegment;

                    LogHelper.printDebug(() -> "Scheduling segment: " + segmentToSkip + " playbackRate: " + playbackRate);
                    final long delayUntilSkip = (long) ((segmentToSkip.start - millis) / playbackRate);
                    ReVancedUtils.runOnMainThreadDelayed(() -> {
                        if (scheduledUpcomingSegment != segmentToSkip) {
                            LogHelper.printDebug(() -> "Ignoring old scheduled segment: " + segmentToSkip);
                            return;
                        }
                        scheduledUpcomingSegment = null;

                        if (!segmentToSkip.timeIsNearStart(VideoInformation.getVideoTime(),
                                videoInformationTimeUpdateThresholdMilliseconds)) {
                            // current video time is not what's expected.  User paused playback
                            LogHelper.printDebug(() -> "Ignoring outdated scheduled segment: " + segmentToSkip);
                            return;
                        }
                        if (segmentToSkip.shouldAutoSkip()) {
                            LogHelper.printDebug(() -> "Running scheduled autoskip segment: " + segmentToSkip);
                            skipSegment(segmentToSkip, segmentToSkip.start, false);
                        } else {
                            LogHelper.printDebug(() -> "Running scheduled show segment: " + segmentToSkip);
                            segmentCurrentlyPlaying = segmentToSkip;
                            SponsorBlockView.showSkipButton(segmentToSkip.category);
                        }
                    }, delayUntilSkip);
                }
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "setVideoTime failure", e);
        }
    }


    private static SponsorSegment lastSegmentSkipped;
    private static long lastSegmentSkippedTime;

    private static void skipSegment(SponsorSegment segment, long currentVideoTime, boolean userManuallySkipped) {
        try {
            // If trying to seek to end of the video, YouTube can seek just short of the actual end.
            // (especially if the video does not end on a whole second boundary).
            // This causes additional segment skip attempts, even though it cannot seek any closer to the desired time.
            // Check for and ignore repeated skip attempts of the same segment over a short time period.
            final long now = System.currentTimeMillis();
            final long minimumMillisecondsBetweenSkippingSameSegment = 500;
            if ((lastSegmentSkipped == segment) && (now - lastSegmentSkippedTime < minimumMillisecondsBetweenSkippingSameSegment)) {
                LogHelper.printDebug(() -> "Ignoring skip segment request (already skipped as close as possible): " + segment);
                return;
            }

            LogHelper.printDebug(() -> "Skipping segment: " + segment);
            lastSegmentSkipped = segment;
            lastSegmentSkippedTime = now;
            segmentCurrentlyPlaying = null;
            scheduledHideSegment = null; // if a scheduled has not run yet
            scheduledUpcomingSegment = null;
            SponsorBlockView.hideSkipButton();

            final boolean seekSuccessful = VideoInformation.seekTo(segment.end);
            if (!seekSuccessful) {
                // can happen when switching videos and is normal
                LogHelper.printDebug(() -> "Could not skip segment (seek unsuccessful): " + segment);
                return;
            }

            if (!userManuallySkipped) {
                // check for any smaller embedded segments, and count those as autoskipped
                final boolean showSkipToast = SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean();
                for (final SponsorSegment otherSegment : segmentsOfCurrentVideo) {
                    if (segment.end <= otherSegment.start) {
                        break; // no other segments can be contained
                    }
                    if (segment.containsSegment(otherSegment)) { // includes checking the segment against itself
                        otherSegment.didAutoSkipped = true; // skipped this segment as well
                        if (showSkipToast) {
                            showSkippedSegmentToast(otherSegment);
                        }
                    }
                }
            }

            if (segment.category == SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
                // skipped segment was a preview of unsubmitted segment
                // remove the segment from the UI view
                SponsorBlockUtils.setNewSponsorSegmentPreviewed();
                SponsorSegment[] newSegments = new SponsorSegment[segmentsOfCurrentVideo.length - 1];
                int i = 0;
                for (SponsorSegment sponsorSegment : segmentsOfCurrentVideo) {
                    if (sponsorSegment != segment)
                        newSegments[i++] = sponsorSegment;
                }
                setSegmentsOfCurrentVideo(newSegments);
            } else {
                SponsorBlockUtils.sendViewRequestAsync(segment, currentVideoTime);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }


    private static int toastNumberOfSegmentsSkipped;
    private static SponsorSegment toastSegmentSkipped;

    private static void showSkippedSegmentToast(SponsorSegment segment) {
        ReVancedUtils.verifyOnMainThread();
        toastNumberOfSegmentsSkipped++;
        if (toastNumberOfSegmentsSkipped > 1) {
            return; // toast already scheduled
        }
        toastSegmentSkipped = segment;

        final long delayToToastMilliseconds = 200; // also the maximum time between skips to be considered skipping multiple segments
        ReVancedUtils.runOnMainThreadDelayed(() -> {
            try {
                if (toastSegmentSkipped == null) { // video was changed just after skipping segment
                    LogHelper.printDebug(() -> "Ignoring old scheduled show toast");
                    return;
                }
                ReVancedUtils.showToastShort(toastNumberOfSegmentsSkipped == 1
                        ? toastSegmentSkipped.category.skipMessage.toString()
                        : str("skipped_multiple_segments"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "showSkippedSegmentToast failure", ex);
            } finally {
                toastNumberOfSegmentsSkipped = 0;
                toastSegmentSkipped = null;
            }
        }, delayToToastMilliseconds);
    }

    public static void onSkipSponsorClicked() {
        if (segmentCurrentlyPlaying != null) {
            skipSegment(segmentCurrentlyPlaying, VideoInformation.getVideoTime(), true);
        } else {
            SponsorBlockView.hideSkipButton();
            LogHelper.printException(() -> "error: segment not available to skip"); // should never happen
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

    /**
     * Injection point
     */
    public static String appendTimeWithoutSegments(String totalTime) {
        try {
            if (SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean()
                    && !TextUtils.isEmpty(totalTime) && VideoInformation.getCurrentVideoLength() > 0) {
                return totalTime + timeWithoutSegments;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "appendTimeWithoutSegments failure", ex);
        }

        return totalTime;
    }

    private static void calculateTimeWithoutSegments() {
        final long currentVideoLength = VideoInformation.getCurrentVideoLength();
        if (!SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean() || currentVideoLength <= 0
                || segmentsOfCurrentVideo == null || segmentsOfCurrentVideo.length == 0) {
            timeWithoutSegments = "";
            return;
        }

        long timeWithoutSegmentsValue = currentVideoLength + 500; // YouTube:tm:
        for (SponsorSegment segment : segmentsOfCurrentVideo) {
            timeWithoutSegmentsValue -= segment.end - segment.start;
        }
        final long hours = timeWithoutSegmentsValue / 3600000;
        final long minutes = (timeWithoutSegmentsValue / 60000) % 60;
        final long seconds = (timeWithoutSegmentsValue / 1000) % 60;
        if (hours > 0) {
            timeWithoutSegments = String.format(" (%d:%02d:%02d)", hours, minutes, seconds);
        } else {
            timeWithoutSegments = String.format(" (%d:%02d)", minutes, seconds);
        }
    }

    /**
     * Injection point
     */
    public static void drawSponsorTimeBars(final Canvas canvas, final float posY) {
        try {
            if (sponsorBarThickness < 0.1) return;
            if (segmentsOfCurrentVideo == null) return;
            final long currentVideoLength = VideoInformation.getCurrentVideoLength();
            if (currentVideoLength <= 0) return;

            final float thicknessDiv2 = sponsorBarThickness / 2;
            final float top = posY - thicknessDiv2;
            final float bottom = posY + thicknessDiv2;
            final float absoluteLeft = sponsorBarLeft;
            final float absoluteRight = sponsorBarRight;

            final float tmp1 = (1f / currentVideoLength) * (absoluteRight - absoluteLeft);
            for (SponsorSegment segment : segmentsOfCurrentVideo) {
                float left = segment.start * tmp1 + absoluteLeft;
                float right = segment.end * tmp1 + absoluteLeft;
                canvas.drawRect(left, top, right, bottom, segment.category.paint);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "drawSponsorTimeBars failure", ex);
        }
    }

}
