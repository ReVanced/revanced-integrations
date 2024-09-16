package app.revanced.integrations.youtube.patches;

import android.content.Intent;
import android.net.Uri;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {
    private static final String MAIN_ACTIONS = "android.intent.action.MAIN";

    /**
     * Injection point.
     */
    public static void changeStartPageLegacy(Intent intent) {
        changeStartPage(intent, true);
    }

    /**
     * Injection point.
     */
    public static void changeStartPage(Intent intent) {
        changeStartPage(intent, false);
    }

    private static void changeStartPage(Intent intent, boolean useUrlStartPages) {
        try {
            Logger.printDebug(() -> "action: " + intent.getAction() + " data: " + intent.getData() + " extras: " + intent.getExtras());

            if (!MAIN_ACTIONS.equals(intent.getAction())) {
                return;
            }

            final var startPage = Settings.START_PAGE.get();
            if (startPage.isEmpty()) return;

            if (startPage.startsWith("open.")) {
                intent.setAction("com.google.android.youtube.action." + startPage);

                Logger.printDebug(() -> "Changed start page shortcut to: " + startPage);
            } else if (useUrlStartPages && startPage.startsWith("www.youtube.com/")) {
                intent.setData(Uri.parse(startPage));

                Logger.printDebug(() -> "Changed start page url to: " + startPage);
            } else {
                Logger.printDebug(() -> "Cannot change start page to: " + startPage);
                Settings.START_PAGE.resetToDefault();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeIntent failure", ex);
        }
    }
}
