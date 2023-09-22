package app.revanced.integrations.utils;

import android.content.Context;

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
        currentNavBarIndex = lastNavBarIndex;
    }

    /**
     * Check if current tab is not Library Tab
     */
    public static boolean isNotLibraryTab() {
        return currentNavBarIndex != 4;
    }
}
