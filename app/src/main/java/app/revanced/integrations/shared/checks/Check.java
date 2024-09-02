package app.revanced.integrations.shared.checks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

import java.util.concurrent.atomic.AtomicReference;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.integrations.shared.StringRef.str;

abstract class Check {
    private static final String REVANCED_LINKS_HTML_TEXT =
            "<>ul>" +
                "<li><a href=https://revanced.app>Website</a>" +
                "<li><a href=https://revanced.app/discord>Discord</a>" +
                "<li><a href=https://www.reddit.com/r/revancedapp>Reddit</a>" +
                "<li><a href=https://twitter.com/revancedapp>Twitter</a>" +
                "<li><a href=https://t.me/app_revanced>Telegram</a>" +
                "<li><a href=https://www.youtube.com/@ReVanced>YouTube</a>" +
            "</ul>";

    private static final int MINIMUM_SECONDS_TO_SHOW_WARNING = 7;

    private static final Uri GOOD_SOURCE = Uri.parse("https://revanced.app");

    private final String failedReasonStringKey;

    /**
     * @param failedReasonStringKey The string key of the reason why the check failed to be shown to the user.
     */
    Check(String failedReasonStringKey) {
        this.failedReasonStringKey = failedReasonStringKey;
    }

    protected abstract boolean run();

    static boolean shouldRun() {
        return Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.get() < 2;
    }

    static void disableForever() {
        Logger.printDebug(() -> "Environment checks disabled forever.");

        Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.save(Integer.MAX_VALUE);
    }

    @SuppressLint("NewApi")
    static void issueWarning(Context context, Check... failedChecks) {
        Utils.verifyOnMainThread();

        final var reasons = new StringBuilder();

        reasons.append("<ul>");
        for (var check : failedChecks) {
            reasons.append("<li>").append(str(check.failedReasonStringKey));
        }
        reasons.append("</ul>");

        var message = Html.fromHtml(
                str("revanced_check_environment_failed_message", reasons.toString(), REVANCED_LINKS_HTML_TEXT),
                FROM_HTML_MODE_COMPACT
        );

        AlertDialog dialog =  new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(str("revanced_check_environment_failed_title"))
                .setMessage(message)
                .setPositiveButton(
                        str("revanced_check_environment_dialog_open_official_source_button"),
                        (dialog1, which) -> {
                            final var intent = new Intent(Intent.ACTION_VIEW, GOOD_SOURCE);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            context.startActivity(intent);
                        }
                ).setNegativeButton(
                        str("revanced_check_environment_dialog_ignore_button"),
                        (dialog1, which) -> {
                            final int current = Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.get();
                            Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.save(current + 1);

                            dialog1.dismiss();
                        }
                ).create();

        dialog.show(); // Must show before getting the dismiss button or setting movement method.

        var dismissButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        dismissButton.setEnabled(false);

        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        // Use a longer delay than any of the other patches that can show a dialog on startup
        // (Announcements, Check watch history), but there is still a chance a slow network
        // can cause the dialogs to be out of order.
        Utils.runOnMainThreadDelayed(getCountdownRunnable(dismissButton), 1000);
    }

    private static Runnable getCountdownRunnable(Button dismissButton) {
        // Don't need atomic, but do need a mutable reference to modify from inside the runnable.
        AtomicReference<Integer> secondsRemainingRef = new AtomicReference<>(MINIMUM_SECONDS_TO_SHOW_WARNING);

        return new Runnable() {
            @Override
            public void run() {
                // Reduce the remaining time by 1 second, but only show the countdown when 3 seconds are left
                // to not draw the user's attention to the dismiss button too early.
                final int secondsRemaining = secondsRemainingRef.get();
                if (secondsRemaining > 0) {
                    if (secondsRemaining <= 3) {
                        dismissButton.setText(str("revanced_check_environment_dialog_ignore_button_countdown", secondsRemaining));
                    }

                    secondsRemainingRef.set(secondsRemaining - 1);

                    Utils.runOnMainThreadDelayed(this, 1000);
                } else {
                    dismissButton.setText(str("revanced_check_environment_dialog_ignore_button"));
                    dismissButton.setEnabled(true);
                }
            }
        };
    }
}
