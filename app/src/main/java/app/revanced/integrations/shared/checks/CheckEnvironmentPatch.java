package app.revanced.integrations.shared.checks;

import static app.revanced.integrations.shared.checks.Check.debugAlwaysShowWarning;
import static app.revanced.integrations.shared.checks.PatchInfo.Build.*;
import static app.revanced.integrations.shared.checks.PatchInfo.MANAGER_PACKAGE_NAME;
import static app.revanced.integrations.shared.checks.PatchInfo.PATCH_TIME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static final boolean DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG = debugAlwaysShowWarning();

    /**
     * Check if the app is installed by the manager, the app store, or thru adb/CLI.
     * <br>
     * Does not conclusively
     * If the app is installed by the manager or the app store, it is likely, the app was patched using the manager,
     * or installed manually via ADB (in the case of ReVanced CLI for example).
     * <br>
     * If the app is not installed by the manager or the app store, then the app was likely downloaded pre-patched
     * and installed by the browser or another unknown app.
     */
    private static class CheckHasExpectedInstallerCheck extends Check {
        /**
         * CLI patching, or manual installation of a previously patched using adb.
         * But excludes adb mounted root installation.
         */
        private static final String ADB_INSTALLATION_PACKAGE_NAME = null;

        private static final List<String> GOOD_INSTALLER_PACKAGE_NAMES = Arrays.asList(
                MANAGER_PACKAGE_NAME,
                "com.android.vending", // Root installation.
                ADB_INSTALLATION_PACKAGE_NAME
        );

        private boolean isNonRootAdbInstallation;

        CheckHasExpectedInstallerCheck() {
            super("revanced_check_environment_manager_not_expected_installer");
        }

        @NonNull
        @Override
        protected Boolean run() {
            final var context = Utils.getContext();

            final var installerPackageName =
                    context.getPackageManager().getInstallerPackageName(context.getPackageName());

            Logger.printInfo(() -> "Installed by: " + installerPackageName);
            final boolean passed = GOOD_INSTALLER_PACKAGE_NAMES.contains(installerPackageName);

            Logger.printInfo(() -> passed
                    ? "Apk was not installed from an unknown source"
                    : "Apk was installed from an unknown source");

            return passed;
        }
    }

    /**
     * Check if the build properties are the same as during the patch.
     * <br>
     * If the build properties are the same as during the patch, it is likely, the app was patched on the same device.
     * <br>
     * If the build properties are different, the app was likely downloaded pre-patched or patched on another device.
     */
    private static class CheckWasPatchedOnSameDeviceCheck extends Check {
        CheckWasPatchedOnSameDeviceCheck() {
            super("revanced_check_environment_not_same_patching_device");
        }

        @SuppressLint({"NewApi", "HardwareIds"})
        @Override
        protected Boolean run() {
            if (PATCH_BOARD.isEmpty()) {
                // Did not patch with Manager, and cannot conclusively say where this was from.
                Logger.printInfo(() -> "APK does not contain a hardware signature and cannot compare to current device");
                return null;
            }

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

            Logger.printInfo(() -> passed
                    ? "Device hardware signature matches current device"
                    : "Device hardware signature does not match current device");

            return passed;
        }
    }

    /**
     * Check if the app was installed within the last 30 minutes after being patched.
     * <br>
     * If the app was installed within the last 30 minutes, it is likely, the app was patched by the user.
     * <br>
     * If the app was installed at a later time, it is likely, the app was downloaded pre-patched, the user
     * waited too long to install the app or the patch time is too long ago.
     */
    private static class CheckIsNearPatchTimeCheck extends Check {
        CheckIsNearPatchTimeCheck() {
            super("revanced_check_environment_not_near_patch_time");
        }

        @NonNull
        @Override
        protected Boolean run() {
            final var durationSincePatching = System.currentTimeMillis() - PATCH_TIME;

            Logger.printInfo(() -> "Installed: " + (durationSincePatching / 1000) + " seconds after patching");

            // Also verify patched time is not in the future.
            return durationSincePatching > 0 && durationSincePatching < 30 * 60 * 1000; // 30 minutes.
        }
    }

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
                Logger.printInfo(() -> "Running environment checks");
                List<Check> failedChecks = new ArrayList<>();

                CheckWasPatchedOnSameDeviceCheck hardwareCheck = new CheckWasPatchedOnSameDeviceCheck();
                boolean deviceSignatureFailed = false;
                Boolean checkResult = hardwareCheck.run();
                if (checkResult != null) {
                    if (checkResult) {
                        if (!DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                            // Patched on the same device using Manager,
                            // and no further checks are needed.
                            Check.disableForever();
                            return;
                        }
                    } else {
                        deviceSignatureFailed = true;
                        failedChecks.add(hardwareCheck);
                    }
                }

                CheckHasExpectedInstallerCheck expectedInstallerCheck = new CheckHasExpectedInstallerCheck();
                checkResult = expectedInstallerCheck.run();
                if (!checkResult) {
                    failedChecks.add(expectedInstallerCheck);
                }

                // Near device time check is ignored for adb installs to prevent false positives.
                // This means all non root adb installs of CLI builds will not be checked,
                // but this seems a highly unlikely way an end user would install a pre-patched app.
                //
                // The goal here is to identify pre-patched installations while
                // never showing false positives for regular use cases.
                CheckIsNearPatchTimeCheck nearTimeCheck = new CheckIsNearPatchTimeCheck();
                checkResult = nearTimeCheck.run();
                // If patched on a different device then a warning will already be shown.
                // But if the patch time was also a while ago then don't bother mentioning it because
                // that issue might be distracting to the the major issue of someone else patching this app.
                if (!checkResult && !deviceSignatureFailed && !expectedInstallerCheck.isNonRootAdbInstallation) {
                    failedChecks.add(nearTimeCheck);
                }

                if (DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                    // Show all failures for debugging layout.
                    failedChecks = Arrays.asList(
                            hardwareCheck,
                            expectedInstallerCheck,
                            nearTimeCheck);
                }

                if (failedChecks.isEmpty()) {
                    Check.disableForever();
                    return;
                }

                Check.issueWarning(
                        context,
                        failedChecks
                );
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
        } catch (NoSuchAlgorithmException ex) {
            Logger.printException(() -> "equalsHash failure", ex); // Will never happen.

            return false;
        }
    }
}
