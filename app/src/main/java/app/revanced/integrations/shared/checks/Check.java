package app.revanced.integrations.shared.checks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.widget.Button;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.integrations.shared.StringRef.str;

abstract class Check {
    private static final Uri GOOD_SOURCE = Uri.parse("https://revanced.app");
    private static final AlertDialog.Builder CHECK_FAILED_DIALOG_BUILDER = new AlertDialog.Builder(Utils.getContext())
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(str("revanced_check_environment_failed_title"))
            .setPositiveButton(str("revanced_check_environment_dialog_open_official_source_button"), (dialog, which) -> {
                final var intent = new Intent(Intent.ACTION_VIEW, GOOD_SOURCE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utils.getContext().startActivity(intent);
            })
            .setNegativeButton(str("revanced_check_environment_dialog_ignore_button"), (dialog, which) -> {
                final int current = Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.get();
                Settings.CHECK_ENVIRONMENT_WARNING_ISSUED_COUNT.save(current + 1);

                dialog.dismiss();
            });


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
    static void issueWarning(Check... failedChecks) {
        final var reasons = new StringBuilder();

        reasons.append("<ul>");

        for (var check : failedChecks) {
            reasons.append("<li>").append(str(check.failedReasonStringKey));
        }

        reasons.append("</ul>");

        final var finalMessage = Html.fromHtml(
                String.format(str("revanced_check_environment_failed_message"), reasons),
                FROM_HTML_MODE_COMPACT
        );
        AlertDialog dialog = CHECK_FAILED_DIALOG_BUILDER.setMessage(finalMessage).create();

        var dismissButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        // TODO: setEnabled called on null? If this line is commented out getting:
        //  Unable to add window -- token null is not valid; is your activity running?
        dismissButton.setEnabled(false);

        dialog.show();

        Utils.runOnMainThread(getCountdownRunnable(dismissButton));
    }

    private static Runnable getCountdownRunnable(Button dismissButton) {
        // Using a reference to an array to be able to modify the value in the Runnable.
        final int[] secondsRemaining = {5};

        return new Runnable() {
            @Override
            public void run() {
                // Reduce the remaining time by 1 second, but only show the countdown when 3 seconds are left
                // to not draw the user's attention to the dismiss button too early.
                if (secondsRemaining[0] > 0) {
                    if (secondsRemaining[0] <= 3) {
                        dismissButton.setText(String.format(
                                str("revanced_check_environment_dialog_ignore_button_countdown"),
                                secondsRemaining[0])
                        );
                    }

                    secondsRemaining[0]--;

                    Utils.runOnMainThreadDelayed(this, 1000);
                } else {
                    dismissButton.setText(str("revanced_check_environment_dialog_ignore_button"));
                    dismissButton.setEnabled(true);
                }
            }
        };
    }
}
