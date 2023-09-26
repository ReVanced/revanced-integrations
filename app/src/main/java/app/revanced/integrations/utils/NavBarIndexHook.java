package app.revanced.integrations.utils;

import android.content.Context;

import app.revanced.integrations.patches.components.SuggestionsShelfFilter;

public class NavBarIndexHook {

    private static int currentNavBarIndex = 0;
    private static int lastNavBarIndex = 0;

    /**
     * Set current NavBar Index to @param
     */
    public static void setCurrentNavBarIndex(int navBarIndex) {
        if (currentNavBarIndex == navBarIndex)
            return;

        LogHelper.printDebug(() -> "Setting current NavBar Index to: " + navBarIndex);
        lastNavBarIndex = currentNavBarIndex;
        currentNavBarIndex = navBarIndex;
    }

    public static void setLastNavBarIndex() {
        // if we come to back to Library fragment from any other like downloads page, playlists, your videos, or your clips
        // NavBar index should still be same
        if (SuggestionsShelfFilter.isLibraryRecentShelfVisible && currentNavBarIndex == 4)
            return;

        // if we come to back to Home fragment from any other like trending, or news, or sports page
        // NavBar index should still be same
        if (SuggestionsShelfFilter.isHomeFeedVisible && currentNavBarIndex == 0)
            return;

        currentNavBarIndex = lastNavBarIndex;
    }

    /**
     * Check if current tab is not Library Tab
     */
    public static boolean isNotLibraryTab() {
        return currentNavBarIndex != 4;
    }
}
