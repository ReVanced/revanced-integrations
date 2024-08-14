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
public class DnsResolverVerificationPatch {

    private static final String HISTORY_TRACKING_ENDPOINT = "s.youtube.com";

    private static final String SINKHOLE_IPV4 = "0.0.0.0";
    private static final String SINKHOLE_IPV6 = "::";

    /** @noinspection SameParameterValue */
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
        if (!Utils.isNetworkConnected() || Settings.IGNORE_DNS_BLOCKER.get()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                if (domainResolvesToValidIP(HISTORY_TRACKING_ENDPOINT)) {
                    return;
                }

                Utils.runOnMainThread(() -> {
                    var alertDialog = new android.app.AlertDialog.Builder(context)
                            .setTitle(str("revanced_dns_resolver_verification_dialog_title"))
                            .setMessage(Html.fromHtml(str("revanced_dns_resolver_verification_dialog_message")))
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                            }).setNegativeButton(str("revanced_dns_resolver_verification_dialog_ignore"), (dialog, which) -> {
                                Settings.IGNORE_DNS_BLOCKER.save(true);
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                });
            } catch (Exception ex) {
                Logger.printException(() -> "checkDnsResolver failure", ex);
            }
        });
    }
}
