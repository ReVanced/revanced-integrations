package app.revanced.integrations.discord.plugin;

import android.app.Activity;
import com.facebook.react.bridge.CatalystInstanceImpl;

@SuppressWarnings("unused")
public final class BunnyBootstrapperPatch {
    private final static BunnyBootstrapper INSTANCE = new BunnyBootstrapper();

    public static void hookOnCreate(Activity mainActivity) {
        INSTANCE.hookOnCreate(mainActivity);
    }

    public static void hookLoadScriptFromFile(CatalystInstanceImpl instance) {
        INSTANCE.hookLoadScriptFromFile(instance);
    }

    public static int hookColorDark(String themeKey, int originalColor) {
        return INSTANCE.hookColorDark(themeKey, originalColor);
    }

    public static int hookColorLight(String themeKey, int originalColor) {
        return INSTANCE.hookColorLight(themeKey, originalColor);
    }

    public static int hookRawColor(Object contextOrResource, int id, int originalColor) {
        return INSTANCE.hookRawColor(contextOrResource, id, originalColor);
    }
}
