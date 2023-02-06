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
            SponsorBlockView.hideSkipButton();
            SponsorBlockView.hideNewSegmentLayout();
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
            if (PlayerType.getCurrent().isNoneOrHidden()) {
                // VideoIdPatch does not yet support shorts. So currently this code will never run
                LogHelper.printDebug(() -> "ignoring shorts video");
                clearDownloadedData();
                return;
            }
            if (!ReVancedUtils.isNetworkConnected()) {
                LogHelper.printDebug(() -> "Network not connected, ignoring video");
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

            ReVancedUtils.runOnMainThread(()-> {
                if (!videoId.equals(currentVideoId)) {
                    // user changed videos before get segments network call could complete
                    LogHelper.printDebug(() -> "Ignoring stale segments for prior video: " + videoId);
                    return;
                }
                setSponsorSegmentsOfCurrentVideo(segments);
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
                || sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) {
                return;
            }
            if (VideoInformation.getCurrentVideoLength() == 0) {
                LogHelper.printDebug(() -> "Video is not yet loaded (video length is 0)."
                        + "  Ignoring setVideoTime call of time: " + millis);
                return;
            }

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
                        break; // no upcoming segments are close enough to schedule a skip

                    if (!segment.shouldAutoSkip())
                        break; // not an autoskip segment, or it's a skip once and has already gone

                    foundUpcomingAutoSkipSegment = true;
                    if (nextSegmentToAutoSkip != segment) {
                        LogHelper.printDebug(() -> "Scheduling segment skip: " + segment);
                        nextSegmentToAutoSkip = segment;

                        // This schedule timing does not account if playback speed is not 1.0x.
                        // If playback speed is not 1.0x, then this scheduled autoskip will later be ignored.
                        // Instead autoskip will happen after playback enters the segment.
                        //
                        // Ideally, this scheduled timing would include the video playback speed,
                        // but getting the playback speed is presently not easily accessible.
                        ReVancedUtils.runOnMainThreadDelayed(() -> {
                            if (nextSegmentToAutoSkip != segment) {
                                LogHelper.printDebug(() -> "Ignoring stale scheduled skip: " + segment);
                                return;
                            }
                            nextSegmentToAutoSkip = null;

                            // If video playback speed is less than 1.0x, then the timer runs too early (at least 250ms too early)
                            // Check if the current playback time is what's expected, and ignore this skip if needed.
                            final long currentVideoTime = VideoInformation.getVideoTime();
                            // use some padding in the time boundary check,
                            // as the scheduled timer might have run a few milliseconds earlier than scheduled
                            // and VideoInformation.getVideoTime() is not exact.
                            final long currentVideoTimeInaccuracyPadding = 120;
                            // Must do this time boundary check here and not in skipSegments(),
                            // otherwise autoskipping multiple segments at once may not appear smoothly
                            if (!segment.timeIsInsideOrNear(currentVideoTime, currentVideoTimeInaccuracyPadding)) {
                                LogHelper.printDebug(() -> "Ignoring skip. Current video time: " + currentVideoTime + " is not close enough to segment: " + segment);
                                return;
                            }
                            LogHelper.printDebug(() -> "Running scheduled skip: " + segment);
                            skipSegment(segment, currentVideoTime, false);
                        }, segment.start - millis);
                    }
                    break;
                }

                if (segment.end <= millis)
                    continue; // already past this segment

                // we are in the segment!
                if (segment.shouldAutoSkip()) {
                    nextSegmentToAutoSkip = null; // if a scheduled skip has not run yet
                    skipSegment(segment, millis, false);
                    break;
                } else {
                    segmentCurrentlyPlayingToManuallySkip = segment;
                    // keep looking. there may be an upcoming autoskip,
                    // or there may be another smaller segment nested inside this segment
                    continue;
                }
            }
            if (!foundUpcomingAutoSkipSegment && nextSegmentToAutoSkip != null) {
                LogHelper.printDebug(() -> "Clearing scheduled segment skip");
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
            skipSegment(segmentCurrentlyPlayingToManuallySkip, VideoInformation.getVideoTime(), true);
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
                SponsorBlockUtils.setPlayerViewGroupContext(viewGroup.getContext());
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

    private static SponsorSegment lastSegmentSkipped;
    private static long lastSegmentSkippedTime;

    private static void skipSegment(SponsorSegment segment, long currentVideoTime, boolean userManuallySkipped) {
        try {
            // If trying to seek to end of the video, YouTube can seek just short of the actual end.
            // (especially if the video does not end on a whole second bound).
            // This causes additional segment skip attempts, even though it cannot seek any closer to the desired time.
            // Check for and ignore repeated skip attempts of the same segment in a short time period.
            final long now = System.currentTimeMillis();
            final long minimumMillisecondsBetweenSkippingSameSegment = 500;
            if ((lastSegmentSkipped == segment) && (now - lastSegmentSkippedTime < minimumMillisecondsBetweenSkippingSameSegment)) {
                LogHelper.printDebug(() -> "Ignoring skip segment request (already skipped as close as possible): " + segment);
                return;
            }

            lastSegmentSkipped = segment;
            lastSegmentSkippedTime = now;
            LogHelper.printDebug(() -> "Skipping segment: " + segment);

            SponsorBlockView.hideSkipButton();

            final boolean seekSuccessful = VideoInformation.seekTo(segment.end);
            if (!seekSuccessful) {
                // can happen when switching videos and is normal
                LogHelper.printDebug(() -> "Could not skip segment (seek unsuccessful): " + segment);
                return;
            }
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
                SponsorBlockUtils.sendViewRequestAsync(currentVideoTime, segment);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }


    private static int toastNumberOfSegmentsSkipped;
    private static SponsorSegment toastSegmentSkipped;

    private static void showSkippedSegmentToast(SponsorSegment segment) {
        ReVancedUtils.verifyOnMainThread();
        if (toastSegmentSkipped == segment) {
            return; // already counted, and the toast is showing soon
        }
        toastNumberOfSegmentsSkipped++;
        if (toastNumberOfSegmentsSkipped > 1) {
            return; // toast already scheduled
        }
        toastSegmentSkipped = segment;

        final long delayToToastMilliseconds = 200; // also the maximum time between skips to be considered skipping multiple segments
        ReVancedUtils.runOnMainThreadDelayed(() -> {
            try {
                if (toastSegmentSkipped == null) { // video was changed just after skipping segment
                    LogHelper.printDebug(() -> "Ignoring stale scheduled show toast");
                    return;
                }
                ReVancedUtils.showToastShort(toastNumberOfSegmentsSkipped == 1
                        ? segment.category.skipMessage.toString()
                        : str("skipped_multiple_segments"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "showSkippedSegmentToast failure", ex);
            } finally {
                toastNumberOfSegmentsSkipped = 0;
                toastSegmentSkipped = null;
            }
        }, delayToToastMilliseconds);
    }

}
