package app.revanced.integrations.sponsorblock;

import android.content.Context;
import android.content.res.Resources;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

// Edit: this is old code from Vanced, and is not currently integrated into ReVanced
public class SwipeHelper {
    static FrameLayout _frameLayout;
    public static boolean isTabletMode;
    public static ViewGroup nextGenWatchLayout;

    public static void SetFrameLayout(Object obj) {
        try {
            _frameLayout = (FrameLayout) obj;
            Context appContext = ReVancedUtils.getContext();
            if (ReVancedUtils.isTablet(appContext) || SharedPrefHelper.getBoolean(SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_swipe_tablet", false)) {
                isTabletMode = true;
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "Unable to set FrameLayout", e);
        }
    }

    public static void setNextGenWatchLayout(Object obj) {
        try {
            nextGenWatchLayout = (ViewGroup) obj;
        } catch (Exception e) {
            LogHelper.printException(() -> "Unable to set _nextGenWatchLayout", e);
        }
    }

    public static boolean IsControlsShown() {
        FrameLayout frameLayout;
        if (isTabletMode || (frameLayout = _frameLayout) == null || frameLayout.getVisibility() != View.VISIBLE) {
            return false;
        }
        if (_frameLayout.getChildCount() > 0) {
            return _frameLayout.getChildAt(0).getVisibility() == View.VISIBLE;
        }
        refreshLayout();
        return false;
    }

    private static void refreshLayout() {
        View findViewById;
        try {
            if (isWatchWhileFullScreen() && (findViewById = nextGenWatchLayout.findViewById(getIdentifier())) != null) {
                _frameLayout = (FrameLayout) findViewById.getParent();
                LogHelper.printDebug(() -> "related_endscreen_results refreshed");
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "Unable to refresh related_endscreen_results layout", e);
        }
    }


    private static boolean isWatchWhileFullScreen() {
        return PlayerType.getCurrent() == PlayerType.WATCH_WHILE_FULLSCREEN;
    }

    private static String getViewMessage(View view) {
        try {
            String resourceName = view.getResources() != null ? view.getId() != 0 ? view.getResources().getResourceName(view.getId()) : "no_id" : "no_resources";
            return "[" + view.getClass().getSimpleName() + "] " + resourceName + "\n";
        } catch (Resources.NotFoundException unused) {
            return "[" + view.getClass().getSimpleName() + "] name_not_found\n";
        }
    }

    private static int getIdentifier() {
        Context appContext = ReVancedUtils.getContext();
        assert appContext != null;
        return appContext.getResources().getIdentifier("related_endscreen_results", "id", appContext.getPackageName());
    }
}
