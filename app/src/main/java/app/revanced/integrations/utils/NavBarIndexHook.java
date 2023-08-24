package app.revanced.integrations.utils;

import android.content.Context;

public class NavBarIndexHook {

    private static int currentNavBarIndex = 0;
    private static boolean isLaunched = false;

    /**
     * Set current NavBar Index to @param
     */
    public static void setCurrentNavBarIndex(int navBarIndex) {
        if (currentNavBarIndex == navBarIndex)
            return;

        LogHelper.printDebug(() -> "Setting current NavBar Index to: " + navBarIndex);
        currentNavBarIndex = navBarIndex;
    }

    /**
     * Check if current tab is Home Tab
     */
    public static boolean isHomeTab() {
        return currentNavBarIndex == 0;
    }

    /**
     * Check if current tab is not Library Tab
     */
    public static boolean isNotLibraryTab() {
        return currentNavBarIndex < 4;
    }

    /**
     * Initialize NavBar Index
     */
    public static void initializeIndex(Context context) {
        if (isLaunched)
            return;

        LogHelper.printDebug(() -> "Initializing NavBar index");
        isLaunched = true;
        setCurrentNavBarIndex(0);
    }
}
