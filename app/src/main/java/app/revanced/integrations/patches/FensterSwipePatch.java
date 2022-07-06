package app.revanced.integrations.patches;

import android.app.Activity;

import androidx.annotation.Nullable;

import app.revanced.integrations.fenster.WatchWhilePlayerType;
import app.revanced.integrations.fenster.controller.FensterController;
import app.revanced.integrations.utils.LogHelper;

/**
 * Hook receiver class for 'FensterV2' video swipe controls.
 *
 * @usedBy app.revanced.patches.youtube.interaction.fenster.patch.FensterPatch
 * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;
 */
@SuppressWarnings("unused")
public final class FensterSwipePatch {
    /**
     * Hook into the main activity lifecycle
     *
     * @param thisRef reference to the WatchWhileActivity instance
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->WatchWhileActivity_onStartHookEX(Ljava/lang/Object;)V
     */
    public static void WatchWhileActivity_onStartHookEX(@Nullable Object thisRef) {
        if (thisRef == null) return;
        if (thisRef instanceof Activity) {
            FensterController.INSTANCE.initialize((Activity) thisRef);
        }
    }

    /**
     * hook into the player overlays lifecycle
     *
     * @param thisRef reference to the PlayerOverlays instance
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_onFinishInflateHookEX(Ljava/lang/Object;)V
     */
    public static void YouTubePlayerOverlaysLayout_onFinishInflateHookEX(@Nullable Object thisRef) {
        //TODO remove
    }

    /**
     * Hook into updatePlayerLayout() method
     *
     * @param type the new player type
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(Ljava/lang/Object;)V
     */
    public static void YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(@Nullable Object type) {
        if (type == null) return;

        //TODO move to own patch?

        // update current player type
        final WatchWhilePlayerType newType = WatchWhilePlayerType.safeParseFromString(type.toString());
        if (newType != null) {
            WatchWhilePlayerType.setCurrent(newType);
            LogHelper.debug(FensterSwipePatch.class, "WatchWhile player type was updated to " + newType);
        }
    }

    /**
     * Hook into NextGenWatchLayout.onTouchEvent
     *
     * @param thisRef     reference to NextGenWatchLayout instance
     * @param motionEvent event parameter
     * @return was the event consumed by the hook?
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->NextGenWatchLayout_onTouchEventHookEX(Ljava/lang/Object;Ljava/lang/Object;)Z
     */
    public static boolean NextGenWatchLayout_onTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        //TODO remove
        return false;
    }

    /**
     * Hook into NextGenWatchLayout.onInterceptTouchEvent
     *
     * @param thisRef     reference to NextGenWatchLayout instance
     * @param motionEvent event parameter
     * @return was the event consumed by the hook?
     * @smali Lapp/revanced/integrations/patches/FensterSwipePatch;->NextGenWatchLayout_onInterceptTouchEventHookEX(Ljava/lang/Object;Ljava/lang/Object;)Z
     */
    public static boolean NextGenWatchLayout_onInterceptTouchEventHookEX(@Nullable Object thisRef, @Nullable Object motionEvent) {
        //TODO remove
        return false;
    }
}
