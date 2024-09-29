package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;

import android.app.Activity;
import android.text.Html;

import java.net.InetAddress;
import java.net.UnknownHostException;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CheckWatchHistoryDomainNameResolutionPatch {

    private static final String HISTORY_TRACKING_ENDPOINT = "s.youtube.com";

    private static final String SINKHOLE_IPV4 = "0.0.0.0";
    private static final String SINKHOLE_IPV6 = "::";

    private static boolean domainResolvesToValidIP(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            String hostAddress = address.getHostAddress();

            if (address.isLoopbackAddress()) {
                Logger.printDebug(() -> host + " resolves to localhost");
            } else if (SINKHOLE_IPV4.equals(hostAddress) || SINKHOLE_IPV6.equals(hostAddress)) {
                Logger.printDebug(() -> host + " resolves to sinkhole ip");
            } else {
                return true; // Domain is not blocked.
            }
        } catch (UnknownHostException e) {
            Logger.printDebug(() -> host + " failed to resolve");
        }

        return false;
    }

    /**
     * Injection point.
     *
     * Checks if s.youtube.com is blacklisted and playback history will fail to work.
     */
    public static void checkDnsResolver(Activity context) {
        if (!Utils.isNetworkConnected() || !Settings.CHECK_WATCH_HISTORY_DOMAIN_NAME.get()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                // If the user has a flaky DNS server, or they just lost internet connectivity
                // and the isNetworkConnected() check has not detected it yet (it can take a few
                // seconds after losing connection), then the history tracking endpoint will
                // show a resolving error but it's actually an internet connection problem.
                //
                // Prevent this false positive by verify youtube.com resolves.
                // If youtube.com does not resolve, then it's not a watch history domain resolving error
                // because the entire app will not work since no domains are resolving.
                if (domainResolvesToValidIP(HISTORY_TRACKING_ENDPOINT)
                        || !domainResolvesToValidIP("youtube.com")) {
                    return;
                }

                Utils.runOnMainThread(() -> {
                    var alert = new android.app.AlertDialog.Builder(context)
                            .setTitle(str("revanced_check_watch_history_domain_name_dialog_title"))
                            .setMessage(Html.fromHtml(str("revanced_check_watch_history_domain_name_dialog_message")))
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                            }).setNegativeButton(str("revanced_check_watch_history_domain_name_dialog_ignore"), (dialog, which) -> {
                                Settings.CHECK_WATCH_HISTORY_DOMAIN_NAME.save(false);
                                dialog.dismiss();
                            }).create();

                    Utils.showDialog(context, alert, false, null);
                });
            } catch (Exception ex) {
                Logger.printException(() -> "checkDnsResolver failure", ex);
            }
        });
    }
}
