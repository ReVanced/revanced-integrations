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

    private static final String YOUTUBE_PLAYBACK_HISTORY_API_ADDRESS = "s.youtube.com";

    /**
     * Injection point.
     *
     * Checks if s.youtube.com is blacklisted and playback history will fail to work.
     */
    public static void checkDnsResolver(Activity context) {
        if (!Utils.isNetworkConnected()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                if (!addressResolvesToLocalhost(YOUTUBE_PLAYBACK_HISTORY_API_ADDRESS)) {
                    return;
                }

                Logger.printDebug(() -> "YouTube history api address resolves to localhost");
            } catch (UnknownHostException e) {
                Logger.printDebug(() -> "YouTube history api address fails to resolve");
            }

            Utils.runOnMainThread(() -> {
                var alertDialog = new android.app.AlertDialog.Builder(context)
                        .setTitle(str("revanced_dns_resolver_verification_dialog_title"))
                        .setMessage(Html.fromHtml(str("revanced_dns_resolver_verification_dialog_message")))
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            dialog.dismiss();
                        }).setNegativeButton(str("revanced_dns_resolver_verification_dialog_ignore"), (dialog, which) -> {
                            Settings.IGNORE_NON_RESOLVING_DNS.save(true);
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .show();
            });
        });
    }

    private static boolean addressResolvesToLocalhost(String host) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        return address.isLoopbackAddress();
    }
}
