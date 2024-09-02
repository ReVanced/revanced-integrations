package app.revanced.integrations.shared.checks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static app.revanced.integrations.shared.checks.PatchInfo.Build.*;
import static app.revanced.integrations.shared.checks.PatchInfo.*;

/**
 * This class is used to check if the app was patched by the user
 * and not downloaded pre-patched, because pre-patched apps are difficult to trust.
 * <br>
 * Various indicators help to detect if the app was patched by the user.
 */
public final class CheckEnvironmentPatch {
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

            //noinspection deprecation
            final var installerPackageName =
                    context.getPackageManager().getInstallerPackageName(context.getPackageName());

            final var passed = GOOD_INSTALLER_PACKAGE_NAMES.contains(installerPackageName);

            if (passed) {
                Logger.printDebug(() -> "Installed by expected installer: " + installerPackageName);
            }

            return passed;
        }
    };

    /**
     * Check if a manager is installed on this device.
     * <br>
     * If the manager is installed, it is likely, the app was patched using the manager.
     * <br>
     * If the manager is not installed, the app was likely downloaded pre-patched,
     * the user patched the app on another device,
     * or the user has uninstalled the manager after patching the app.
     */
    // This check may need the app to have permission to query the manager app, in case the app is targeting A11+.
    private static final Check isManagerInstalledCheck = new Check(
            "revanced_check_environment_manager_not_installed"
    ) {
        @Override
        protected boolean run() {
            try {
                //noinspection deprecation
                Utils.getContext().getPackageManager().getPackageInfo(MANAGER_PACKAGE_NAME, 0);

                Logger.printDebug(() -> "Manager is installed");

                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.printDebug(() -> "Could not find manager package: " + e.getMessage());

                return false;
            }
        }
    };

    /**
     * Check if the build properties are the same as during the patch.
     * <br>
     * If the build properties are the same as during the patch, it is likely, the app was patched on the same device.
     * <br>
     * If the build properties are different, the app was likely downloaded pre-patched or patched on another device.
     */
    private static final Check isPatchTimeBuildCheck = new Check(
            "revanced_check_environment_not_patch_time_build"
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

            if (passed) {
                Logger.printDebug(() -> "Build properties match current device");
            }

            return passed;
        }
    };

    /**
     * Check if the app was installed within the last 30 minutes after being patched.
     * <br>
     * If the app was installed within the last 30 minutes, it is likely, the app was patched by the user.
     * <br>
     * If the app was installed at a later time, it is likely, the app was downloaded pre-patched, the user
     * waited too long to install the app or the patch time is too long ago.
     */
    private static final Check isNearPatchTimeCheck = new Check(
            "revanced_check_environment_not_near_patch_time"
    ) {
        @Override
        protected boolean run() {
            final var duration = System.currentTimeMillis() - PATCH_TIME;

            final var passed = duration < 1000 * 60 * 30; // 1000 ms * 60 s * 30 min

            if (passed) {
                Logger.printDebug(() -> "Installed in " + duration / 1000 + " seconds after patch");
            }

            return passed;
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
            "revanced_check_environment_not_patch_time_public_ip"
    ) {
        @Override
        protected boolean run() {
            // Using multiple services to increase reliability, distribute the load and minimize tracking.
            final var getIpServices = new ArrayList<String>() {
                {
                    add("https://wtfismyip.com/text");
                    add("https://whatsmyfuckingip.com/text");
                    add("https://api.ipify.org?format=text");
                    add("https://icanhazip.com");
                    add("https://ifconfig.me/ip");
                }
            };

            String publicIP = null;

            try {
                var service = getIpServices.get(new Random().nextInt(getIpServices.size() - 1));

                Logger.printDebug(() -> "Using " + service + " to get public IP");

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(service).openConnection();

                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    publicIP = in.readLine();

                    String finalPublicIP = publicIP;
                    Logger.printDebug(() -> "Got public IP " + finalPublicIP);

                    in.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                // If the app does not have the INTERNET permission or the service is down,
                // the public IP can not be retrieved.
                Logger.printDebug(() -> "Failed to get public IP address: " + e.getMessage());
            }

            if (publicIP == null) {
                return false;
            }

            final var passed = equalsHash(
                    // Use last three digits to prevent brute forcing the hashed IP.
                    publicIP.substring(publicIP.length()-3),
                    PUBLIC_IP_DURING_PATCH
            );

            if (passed) {
                Logger.printDebug(() -> "Public IP check passed: " + passed);
            }

            return passed;
        }
    };


    public static void check(Activity context) {
        Logger.printDebug(() -> "Running environment checks");

        // If the warning was already issued twice, or if the check was successful in the past,
        // do not run the checks again.
        if (!Check.shouldRun()) {
            Logger.printDebug(() -> "Environment checks should not run anymore");
            return;
        }

        // TODO: There should be a better way to run the checks. Running all in parallel is not ideal,
        //  because you may run checks that could be exempted by others
        //  such as isPatchTimePublicIPCheck when isNearPatchTimeCheck was already successful.
        //  Also, a warning should be issued for failed checks that result in the entire environment check failing,
        //  but currently, the warning is only issued, when all checks fail.

        if (isNearPatchTimeCheck.run() || isPatchTimeBuildCheck.run()) {
            Logger.printDebug(() -> "Passed first checks");
            Check.disableForever();
            return;
        }

        Logger.printDebug(() -> "Failed first checks");

        if (isManagerInstalledCheck.run() || hasExpectedInstallerCheck.run()) {
            Logger.printDebug(() -> "Passed second checks");
            Check.disableForever();
            return;
        }

        Logger.printDebug(() -> "Failed second checks");

        if (hasPatchTimePublicIPCheck.run()) {
            Logger.printDebug(() -> "Passed third checks");
            Check.disableForever();
            return;
        }

        Logger.printDebug(() -> "Failed all checks");

        Check.issueWarning(
                context,
                isNearPatchTimeCheck,
                isPatchTimeBuildCheck,
                isManagerInstalledCheck,
                hasExpectedInstallerCheck,
                hasPatchTimePublicIPCheck
        );
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
