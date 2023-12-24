package app.revanced.integrations.shared.settings;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import app.revanced.integrations.tiktok.settings.SettingsEnum;

/**
 * Class is used across multiple target apps.
 *
 * This entire class can _not_ reference:
 * {@link app.revanced.integrations.settings.SettingsEnum}
 * {@link app.revanced.integrations.twitch.settings.SettingsEnum}
 * {@link SettingsEnum}
 *
 * or any other code that references these app specific integration classes.
 */
public class SettingsUtils {

    public static void restartApp(@NonNull Context context) {
        String packageName = context.getPackageName();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
        // Required for API 34 and later
        // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
        mainIntent.setPackage(packageName);
        context.startActivity(mainIntent);
        System.exit(0);
    }
}
