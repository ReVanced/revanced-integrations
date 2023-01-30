package app.revanced.integrations.sponsorblock;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class PlayerController {

    private static final Timer sponsorTimer = new Timer("sponsor-skip-timer");
    // fields must be volatile, as they are read/wright from different threads (timer thread and main thread)
    @Nullable
    private static volatile String currentVideoId;
    @Nullable
    private static volatile SponsorSegment[] sponsorSegmentsOfCurrentVideo;
    private static volatile String timeWithoutSegments = "";
    private static volatile long allowNextSkipRequestTime = 0L;
    private static volatile long lastKnownVideoTime = -1L;
    private static final Runnable findAndSkipSegmentRunnable = () -> {
        findAndSkipSegment(false);
    };
    private static volatile SponsorSegment segmentToSkip;
    private static volatile boolean settingsInitialized;
    // UI fields should be accessed exclusively on a single thread (main thread). volatile is not needed
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
        segmentToSkip = null; // prevent any existing scheduled skip from running
    }

    /**
     * Injection point.
     * Called when creating some kind of youtube internal player, every time when new video starts to play
     */
    public static void initialize(Object _o) {
        try {
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
            LogHelper.printDebug(() -> "setCurrentVideoId: " + videoId);

            sponsorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        executeDownloadSegments(videoId);
                    } catch (Exception e) {
                        LogHelper.printException(() -> "Failed to download segments", e);
                    }
                }
            }, 0);
        } catch (Exception ex) {
            LogHelper.printException(() -> "setCurrentVideoId failure", ex);
        }
    }

    static void executeDownloadSegments(String videoId) {
        try {
            SponsorSegment[] segments = SBRequester.getSegments(videoId);

            if (!videoId.equals(currentVideoId)) {
                // user changed videos before get segments network call could complete
                LogHelper.printDebug(() -> "ignoring stale segments for prior video: " + videoId);
                return;
            }

            setSponsorSegmentsOfCurrentVideo(segments);
            ReVancedUtils.runOnMainThread(findAndSkipSegmentRunnable); // skip any segments currently in

            LogHelper.printDebug(() -> {
                StringBuilder builder = new StringBuilder("Downloaded segments:");
                for (SponsorSegment segment : segments) {
                    builder.append('\n').append(segment);
                }
                return builder.toString();
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "executeDownloadSegments failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static void setVideoTime(long millis) {
        try {
            if (!SettingsEnum.SB_ENABLED.getBoolean()) return;

            lastKnownVideoTime = millis;
            //if (millis <= 0) return;
            //findAndSkipSegment(false);

            SponsorSegment[] segments = sponsorSegmentsOfCurrentVideo;
            if (segments == null || segments.length == 0) return;

            LogHelper.printDebug(() -> "setCurrentVideoTime: current video time: " + millis);

            if (isAtEndOfVideo()) {
                ShieldButton.hide();
                VotingButton.hide();
            }

            // to debug the segmentToSkip stale detection, set this to a very large value (12,000 or more)
            // then manually seek to a different location just before an autoskip segment starts
            final long START_TIMER_BEFORE_SEGMENT_MILLIS = 1200;
            final long startTimerAtMillis = millis + START_TIMER_BEFORE_SEGMENT_MILLIS;

            for (final SponsorSegment segment : segments) {
                if (millis < segment.start) { // segment is upcoming
                    if (startTimerAtMillis < segment.start)
                        break; // not inside any segments, and no upcoming segments are close enough to schedule a task
                    if (!segment.category.behaviour.skip)
                        break;

                    if (segmentToSkip != segment) {
                        LogHelper.printDebug(() -> "scheduling segmentToSkip");
                        TimerTask skipSponsorTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (segmentToSkip != segment) {
                                    LogHelper.printDebug(() -> "ignoring stale segmentToSkip task");
                                } else {
                                    lastKnownVideoTime = segment.start + 1;
                                    ReVancedUtils.runOnMainThread(findAndSkipSegmentRunnable);
                                }
                            }
                        };
                        sponsorTimer.schedule(skipSponsorTask, segment.start - millis);
                    } else {
                        LogHelper.printDebug(() -> "skipSponsorTask is already scheduled...");
                    }
                    SkipSegmentView.hide();
                    return;
                }

                if (segment.end < millis)
                    continue; // already past this segment

                // we are in the segment!
                if (!segment.category.behaviour.skip) {
                    continue; // this segment does not skip, but maybe another overlaps and does
                }
                if (segment.category.behaviour.key.equals("skip-once") && segment.didAutoSkipped) {
                    SkipSegmentView.show(); // already auto skipped once
                    return;
                }
                skipSegment(segment, false);
                SponsorBlockUtils.sendViewRequestAsync(millis, segment);
                break;
            }
            // nothing upcoming to skip and not in a segment. clear any old skip tasks and hide the skip segment view
            segmentToSkip = null;
            SkipSegmentView.hide();
        } catch (Exception e) {
            LogHelper.printException(() -> "setVideoTime failure", e);
        }
    }

    /**
     * Injection point
     */
    public static void setHighPrecisionVideoTime(final long millis) {
        try {
            lastKnownVideoTime = millis;
        } catch (Exception ex) {
            LogHelper.printException(() -> "setHighPrecisionVideoTime failure", ex);
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
        LogHelper.printDebug(() -> "Skip segment clicked");
        findAndSkipSegment(true);
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
        SponsorSegment[] currentSegments = sponsorSegmentsOfCurrentVideo;
        if (!SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean() || currentVideoLength <= 1
                || currentSegments == null || currentSegments.length == 0) {
            timeWithoutSegments = "";
            return;
        }

        long timeWithoutSegmentsValue = currentVideoLength + 500; // YouTube:tm:
        for (SponsorSegment segment : currentSegments) {
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
            SponsorSegment[] currentSegments = PlayerController.sponsorSegmentsOfCurrentVideo;
            if (currentSegments == null) return;

            final float thicknessDiv2 = sponsorBarThickness / 2;
            final float top = posY - thicknessDiv2;
            final float bottom = posY + thicknessDiv2;
            final float absoluteLeft = sponsorBarLeft;
            final float absoluteRight = sponsorBarRight;

            final float tmp1 = 1f / (float) VideoInformation.getCurrentVideoLength() * (absoluteRight - absoluteLeft);
            for (SponsorSegment segment : currentSegments) {
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

    private static void findAndSkipSegment(boolean userManuallySkipped) {
        try {
            SponsorSegment[] currentSegments = sponsorSegmentsOfCurrentVideo;
            if (currentSegments == null)
                return;

            final long millis = lastKnownVideoTime;

            for (SponsorSegment segment : currentSegments) {
                if (millis < segment.start)
                    break; // this and all remaining segments are later

                if (segment.end <= millis)
                    continue; // past this segment

                if (!segment.category.behaviour.skip)
                    continue; // not a segment to skip, but maybe other segments overlap

                if (!userManuallySkipped && segment.didAutoSkipped && segment.category.behaviour.key.equals("skip-once")) {
                    SkipSegmentView.show();
                    return;
                }

                // inside the segment
                skipSegment(segment, userManuallySkipped);
                SponsorBlockUtils.sendViewRequestAsync(millis, segment);
                break;
            }

            SkipSegmentView.hide();
        } catch (Exception ex) {
            LogHelper.printException(() -> "findAndSkipSegment failure", ex);
        }
    }

    private static void skipSegment(SponsorSegment segment, boolean userManuallySkipped) {
        try {
            LogHelper.printDebug(() -> "Skipping segment: " + segment.toString());

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
                SponsorSegment[] currentSegments = sponsorSegmentsOfCurrentVideo;
                SponsorSegment[] newSegments = new SponsorSegment[currentSegments.length - 1];
                int i = 0;
                for (SponsorSegment sponsorSegment : currentSegments) {
                    if (sponsorSegment != segment)
                        newSegments[i++] = sponsorSegment;
                }
                setSponsorSegmentsOfCurrentVideo(newSegments);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }
}
