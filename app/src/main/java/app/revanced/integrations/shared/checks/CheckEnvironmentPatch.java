package app.revanced.integrations.shared.checks;

import static app.revanced.integrations.shared.checks.PatchInfo.Build.*;
import static app.revanced.integrations.shared.checks.PatchInfo.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

/**
 * This class is used to check if the app was patched by the user
 * and not downloaded pre-patched, because pre-patched apps are difficult to trust.
 * <br>
 * Various indicators help to detect if the app was patched by the user.
 */
@SuppressWarnings("unused")
public final class CheckEnvironmentPatch {
    /**
     * For debugging and development only.
     * Forces all checks to be performed, and the check failed dialog to be shown.
     */
    private static final boolean DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG = false;

    /**
     * Check if the app is installed by the manager or the app store.
     * <br>
     * If the app is installed by the manager or the app store, it is likely, the app was patched using the manager,
     * or installed manually via ADB (in the case of ReVanced CLI for example).
     * <br>
     * If the app is not installed by the manager or the app store, then the app was likely downloaded pre-patched
     * and installed by the browser or another unknown app.
     */
    private static final Check hasExpectedInstallerCheck = new Check(
            "revanced_check_environment_manager_not_expected_installer"
    ) {
        final Set<String> GOOD_INSTALLER_PACKAGE_NAMES = new HashSet<>() {
            {
                add(MANAGER_PACKAGE_NAME);
                add("com.android.vending");
            }
        };

        @Override
        protected boolean run() {
            final var context = Utils.getContext();

            final var installerPackageName =
                    context.getPackageManager().getInstallerPackageName(context.getPackageName());

            Logger.printDebug(() -> "Installed by " + installerPackageName);

            return GOOD_INSTALLER_PACKAGE_NAMES.contains(installerPackageName);
        }
    };

    /**
     * Check if the build properties are the same as during the patch.
     * <br>
     * If the build properties are the same as during the patch, it is likely, the app was patched on the same device.
     * <br>
     * If the build properties are different, the app was likely downloaded pre-patched or patched on another device.
     */
    private static final Check isPatchingDeviceSameCheck = new Check(
            "revanced_check_environment_not_same_patching_device"
    ) {
        @SuppressLint({"NewApi", "HardwareIds"})
        @Override
        protected boolean run() {
            //noinspection deprecation
            final var passed = equalsHash(Build.BOARD, PATCH_BOARD) &&
                    equalsHash(Build.BOOTLOADER, PATCH_BOOTLOADER) &&
                    equalsHash(Build.BRAND, PATCH_BRAND) &&
                    equalsHash(Build.CPU_ABI, PATCH_CPU_ABI) &&
                    equalsHash(Build.CPU_ABI2, PATCH_CPU_ABI2) &&
                    equalsHash(Build.DEVICE, PATCH_DEVICE) &&
                    equalsHash(Build.DISPLAY, PATCH_DISPLAY) &&
                    equalsHash(Build.FINGERPRINT, PATCH_FINGERPRINT) &&
                    equalsHash(Build.HARDWARE, PATCH_HARDWARE) &&
                    equalsHash(Build.HOST, PATCH_HOST) &&
                    equalsHash(Build.ID, PATCH_ID) &&
                    equalsHash(Build.MANUFACTURER, PATCH_MANUFACTURER) &&
                    equalsHash(Build.MODEL, PATCH_MODEL) &&
                    equalsHash(Build.ODM_SKU, PATCH_ODM_SKU) &&
                    equalsHash(Build.PRODUCT, PATCH_PRODUCT) &&
                    equalsHash(Build.RADIO, PATCH_RADIO) &&
                    equalsHash(Build.SERIAL, PATCH_SERIAL) &&
                    equalsHash(Build.SKU, PATCH_SKU) &&
                    equalsHash(Build.SOC_MANUFACTURER, PATCH_SOC_MANUFACTURER) &&
                    equalsHash(Build.SOC_MODEL, PATCH_SOC_MODEL) &&
                    equalsHash(Build.TAGS, PATCH_TAGS) &&
                    equalsHash(Build.TYPE, PATCH_TYPE) &&
                    equalsHash(Build.USER, PATCH_USER);

            Logger.printDebug(() -> passed
                    ? "Device properties match current device"
                    : "Device properties do not match current device");

            return passed;
        }
    };

    /**
     * Check if the app was installed within the last 15 minutes after being patched.
     * <br>
     * If the app was installed within the last 15 minutes, it is likely, the app was patched by the user.
     * <br>
     * If the app was installed at a later time, it is likely, the app was downloaded pre-patched, the user
     * waited too long to install the app or the patch time is too long ago.
     */
    private static final Check isNearPatchTimeCheck = new Check(
            "revanced_check_environment_not_near_patch_time"
    ) {
        @Override
        protected boolean run() {
            final var durationSincePatching = System.currentTimeMillis() - PATCH_TIME;

            Logger.printDebug(() -> "Installed: " + (durationSincePatching / 1000) + " seconds after patching");

            // Also verify patched time is not in the future.
            return durationSincePatching > 0 && durationSincePatching < 15 * 60 * 1000; // 15 minutes.
        }
    };

    /**
     * Check if the IP address is the same as during the patch.
     * <br>
     * If the IP address is the same as during the patch, it is likely, the app was just patched by the user.
     * <br>
     * If the IP address is different, the app was likely downloaded pre-patched or the patch time is too long ago.
     */
    private static final Check hasPatchTimePublicIPCheck = new Check(
            "revanced_check_environment_not_same_patch_time_network_address"
    ) {
        @Override
        protected boolean run() {
            Utils.verifyOffMainThread();
            if (!Utils.isNetworkConnected()) return false;

            // Using multiple services to increase reliability, distribute the load and minimize tracking.
            String[] getIpServices = {
                    "https://wtfismyip.com/text",
                    "https://whatsmyfuckingip.com/text",
                    "https://api.ipify.org?format=text",
                    "https://icanhazip.com",
                    "https://ifconfig.me/ip"
            };

            // Use a random service, and fallback on others if one fails.
            Collections.shuffle(Arrays.asList(getIpServices));

            for (String service : getIpServices) {
                String publicIP = null;
                HttpURLConnection urlConnection = null;
                try {
                    Logger.printDebug(() -> "Getting public ip using: " + service);

                    urlConnection = (HttpURLConnection) new URL(service).openConnection();
                    urlConnection.setFixedLengthStreamingMode(0);
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        publicIP = in.readLine();
                        //noinspection ResultOfMethodCallIgnored
                        InetAddress.getByName(publicIP); // Validate IP address.
                    }
                } catch (UnknownHostException ex) {
                    String finalPublicIp = publicIP;
                    Logger.printDebug(() -> "IP Service returned junk data:" + finalPublicIp, ex);
                    continue;
                } catch (Exception ex) {
                    // If the app does not have the INTERNET permission or the service is down,
                    // the public IP can not be retrieved.
                    Logger.printDebug(() -> "Failed to get public IP address", ex);
                    continue;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

                final var passed = equalsHash(
                        // Use last five characters to prevent brute forcing the hashed IP.
                        publicIP.substring(publicIP.length() - 5),
                        PUBLIC_IP_DURING_PATCH
                );

                Logger.printDebug(() -> passed
                        ? "Public IP matches patch time public IP"
                        : "Public IP does not match patch time public IP");

                return passed;
            }

            return false;
        }
    };

    /**
     * Injection point.
     */
    public static void check(Activity context) {
        // If the warning was already issued twice, or if the check was successful in the past,
        // do not run the checks again.
        if (!Check.shouldRun() && !DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
            Logger.printDebug(() -> "Environment checks are disabled");
            return;
        }

        Utils.runOnBackgroundThread(() -> {
            try {
                Logger.printDebug(() -> "Running environment checks");

                if (isNearPatchTimeCheck.run() || isPatchingDeviceSameCheck.run()) {
                    Logger.printDebug(() -> "Passed build time check");
                    if (!DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        Check.disableForever();
                        return;
                    }
                }

                Logger.printDebug(() -> "Failed build time check");

                if (hasExpectedInstallerCheck.run()) {
                    Logger.printDebug(() -> "Passed installation source check");
                    if (!DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        Check.disableForever();
                        return;
                    }
                }

                Logger.printDebug(() -> "Failed second checks");

                if (hasPatchTimePublicIPCheck.run()) {
                    Logger.printDebug(() -> "Passed network address check");
                    if (!DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        Check.disableForever();
                        return;
                    }
                }

                Logger.printDebug(() -> "Failed all checks");

                Utils.runOnMainThread(() -> {
                    Check.issueWarning(
                            context,
                            isNearPatchTimeCheck,
                            isPatchingDeviceSameCheck,
                            hasExpectedInstallerCheck,
                            hasPatchTimePublicIPCheck
                    );
                });
            } catch (Exception ex) {
                Logger.printException(() -> "check failure", ex);
            }
        });
    }

    private static boolean equalsHash(String value, String hash) {
        if (value == null) {
            Logger.printDebug(() -> "Value is null");

            return false;
        }

        try {
            final var sha1 = MessageDigest.getInstance("SHA-1").digest(value.getBytes());
            return Base64.encodeToString(sha1, Base64.DEFAULT).equals(hash);
        } catch (NoSuchAlgorithmException e) {
            Logger.printException(() -> "Failed to value");

            return false;
        }
    }
}
