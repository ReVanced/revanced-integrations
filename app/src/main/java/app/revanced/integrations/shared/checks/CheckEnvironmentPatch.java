package app.revanced.integrations.shared.checks;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.shared.checks.Check.debugAlwaysShowWarning;
import static app.revanced.integrations.shared.checks.PatchInfo.Build.*;
import static app.revanced.integrations.shared.checks.PatchInfo.PATCH_TIME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private static final boolean DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG = debugAlwaysShowWarning();

    private enum InstallationType {
        /**
         * CLI patching, manual installation of a previously patched using adb,
         * or root installation if stock app is first installed using adb.
         */
        ADB(null),
        ROOT_MOUNT_ON_APP_STORE("com.android.vending"),
        MANAGER("app.revanced.manager.flutter");

        @Nullable
        static InstallationType installTypeFromPackageName(@Nullable String packageName) {
            for (InstallationType type : values()) {
                if (Objects.equals(type.packageName, packageName)) {
                    return type;
                }
            }

            return null;
        }

        @Nullable
        final String packageName;

        InstallationType(@Nullable String packageName) {
            this.packageName = packageName;
        }
    }

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
    private static class CheckExpectedInstaller extends Check {
        @Nullable
        InstallationType installFound;

        @NonNull
        @Override
        protected Boolean check() {
            final var context = Utils.getContext();

            final var installerPackageName =
                    context.getPackageManager().getInstallerPackageName(context.getPackageName());

            Logger.printInfo(() -> "Installed by: " + installerPackageName);

            installFound = InstallationType.installTypeFromPackageName(installerPackageName);
            final boolean passed = (installFound != null);

            Logger.printInfo(() -> passed
                    ? "Apk was not installed from an unknown source"
                    : "Apk was installed from an unknown source");

            return passed;
        }

        @Override
        protected String failureReason() {
            return str("revanced_check_environment_manager_not_expected_installer");
        }
    }

    /**
     * Check if the build properties are the same as during the patch.
     * <br>
     * If the build properties are the same as during the patch, it is likely, the app was patched on the same device.
     * <br>
     * If the build properties are different, the app was likely downloaded pre-patched or patched on another device.
     */
    private static class CheckWasPatchedOnSameDevice extends Check {
        @SuppressLint({"NewApi", "HardwareIds"})
        @Override
        protected Boolean check() {
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

        @Override
        protected String failureReason() {
            return str("revanced_check_environment_not_same_patching_device");
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
    private static class CheckIsNearPatchTime extends Check {
        static final int THRESHOLD_FOR_PATCHING_RECENTLY = 30 * 60 * 1000;  // 30 minutes.
        final long DURATION_SINCE_PATCHING = System.currentTimeMillis() - PATCH_TIME;

        @NonNull
        @Override
        protected Boolean check() {
            Logger.printInfo(() -> "Installed: " + (DURATION_SINCE_PATCHING / 1000) + " seconds after patching");

            // Also verify patched time is not in the future.
            return DURATION_SINCE_PATCHING > 0 && DURATION_SINCE_PATCHING < THRESHOLD_FOR_PATCHING_RECENTLY;
        }

        @Override
        protected String failureReason() {
            if (DURATION_SINCE_PATCHING < 0) {
                // Could happen if the user has their device clock incorrectly set in the past,
                // but assume that isn't the case and the apk was patched on a device with the wrong system time.
                return str("revanced_check_environment_not_near_patch_time_invalid");
            }

            // If patched over 1 day ago, show how old this pre-patched apk is.
            // Showing the age can help convey it's better to patch yourself and know it's the latest.
            final long oneDay = 24 * 60 * 60 * 1000;
            final long daysSincePatching = DURATION_SINCE_PATCHING / oneDay;
            if (daysSincePatching > 1) { // Use over 1 day to avoid singular vs plural strings.
                return str("revanced_check_environment_not_near_patch_time_days", daysSincePatching);
            }

            return str("revanced_check_environment_not_near_patch_time");
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

                CheckWasPatchedOnSameDevice sameHardware = new CheckWasPatchedOnSameDevice();
                Boolean hardwareCheckPassed = sameHardware.check();
                if (hardwareCheckPassed != null) {
                    if (hardwareCheckPassed && !DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                        // Patched on the same device using Manager,
                        // and no further checks are needed.
                        Check.disableForever();
                        return;
                    } else {
                        failedChecks.add(sameHardware);
                    }
                }

                CheckExpectedInstaller installerCheck = new CheckExpectedInstaller();
                if (!installerCheck.check() || installerCheck.installFound == InstallationType.MANAGER) {
                    // If the installer package is Manager but the code reached here,
                    // that means it must not be the right Manager otherwise the hardware hash
                    // signatures would be present and this further check would not have run.
                    failedChecks.clear();
                    failedChecks.add(installerCheck);

                    // If the installer check failed, then this
                    // install also must have been patched on a different device.
                    failedChecks.add(sameHardware);
                }

                // Show this failure only if other checks also failed.
                // Otherwise clearing the app data and relaunching could show this error again.
                //
                // Near device time check is ignored for adb installs to prevent false positives.
                // This means all adb installs of CLI builds will always pass,
                // but it seems highly unlikely an end user would install a pre-patched app using adb.
                CheckIsNearPatchTime nearPatchTime = new CheckIsNearPatchTime();
                if (!nearPatchTime.check() && !failedChecks.isEmpty()
                        && installerCheck.installFound != InstallationType.ADB) {
                    failedChecks.add(nearPatchTime);
                }

                if (DEBUG_ALWAYS_SHOW_CHECK_FAILED_DIALOG) {
                    // Show all failures for debugging layout.
                    failedChecks = Arrays.asList(
                            installerCheck,
                            sameHardware,
                            nearPatchTime);
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
