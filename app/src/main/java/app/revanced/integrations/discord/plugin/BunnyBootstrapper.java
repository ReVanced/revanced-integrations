
package app.revanced.integrations.discord.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import app.revanced.integrations.shared.react.BaseRemoteReactPreloadScriptBootstrapper;
import com.facebook.react.bridge.CatalystInstanceImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;


public class BunnyBootstrapper extends BaseRemoteReactPreloadScriptBootstrapper {
    private WeakReference<Context> context;

    private JSONObject theme;
    private final HashMap<String, Integer> RESOURCE_COLORS = new HashMap<>();
    private final HashMap<String, int[]> COMPONENT_COLORS = new HashMap<>();


    @Override
    protected void initialize(Context context) {
        this.context = new WeakReference<>(context);

        download(
                "https://raw.githubusercontent.com/pyoncord/detta-builds/main/bunny.js",
                getWorkingDirectoryFile("bunny.bundle"),
                1024
        );

        readThemeFile();
    }

    @Override
    public void loadPreloadScripts(CatalystInstanceImpl instance) {
        var config = new JSONObject();
        try {
            config.put("loaderName", "ReVanced");
            config.put("loaderVersion", "1.0.0");
            config.put("hasThemeSupport", true);
            buildThemeConfig(config);
            buildSysColorsConfig(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        instance.setGlobalVariable("__PYON_LOADER__", config.toString());
        super.loadPreloadScripts(instance);
    }

    private void buildThemeConfig(JSONObject config) throws JSONException {
        config.put("storedTheme", theme);
    }

    private void buildSysColorsConfig(JSONObject config) throws JSONException {
        boolean isSystemColorsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

        config.put("isSysColorsSupported", isSystemColorsSupported);

        if (isSystemColorsSupported) {
            var context = this.context.get();
            var resources = context.getResources();
            var packageName = context.getPackageName();

            String[] accents = {"accent1", "accent2", "accent3", "neutral1", "neutral2"};
            int[] shades = {0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

            var colors = new JSONObject() {{
                for (String accent : accents) {
                    var accentColors = new JSONArray() {{
                        for (int shade : shades) {
                            @SuppressLint("DiscouragedApi")
                            var colorResourceId = resources.getIdentifier(
                                    "system_" + accent + "_" + shade,
                                    "color",
                                    packageName
                            );
                            var color = colorResourceId == 0 ? 0 : context.getColor(colorResourceId);
                            var hexColor = String.format("#%06X", (0xFFFFFF & color));

                            put(hexColor);
                        }
                    }};

                    put(accent, accentColors);
                }
            }};

            config.put("sysColors", colors);
        }
    }

    public int hookColorDark(String themeKey, int originalColor) {
        return getColor(themeKey, originalColor, true);
    }

    public int hookColorLight(String themeKey, int originalColor) {
        return getColor(themeKey, originalColor, false);
    }

    public int hookRawColor(Object contextOrResource, int id, int originalColor) {
        return readRawColor(contextOrResource, id, originalColor);
    }

    private int getColor(String colorName, int originalColor, boolean isDark) {
        waitUntilInitialized();

        var colors = COMPONENT_COLORS.get(colorName);
        if (colors == null) {
            return originalColor;
        }

        if (isDark) {
            return colors[0];
        }

        // Only if there are two colors in the array we return the light color
        if (colors.length == 2) {
            return colors[1];
        }

        return originalColor;
    }

    private void readThemeFile() {
        var themeFile = getWorkingDirectoryFile("pyoncord/theme.json");
        var legacyThemeFile = getWorkingDirectoryFile("vendetta_theme.json");

        if (legacyThemeFile.exists() && !legacyThemeFile.renameTo(themeFile)) {
            throw new RuntimeException("Failed to rename theme file");
        }

        if (!themeFile.exists()) {
            return;
        }

        try {
            theme = new JSONObject(read(themeFile, 256));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        readThemeColors();
    }

    private int hexStringToColorInt(String hexString) {
        var parsed = Color.parseColor(hexString);
        return (hexString.length() == 7) ? parsed : parsed & 0xFFFFFF | (parsed >>> 24);
    }

    private void readThemeColors() {
        try {
            var data = theme.getJSONObject("data");

            var jsonRawColors = data.getJSONObject("rawColors");
            var jsonSemanticColors = data.getJSONObject("semanticColors");

            for (var colors = jsonRawColors.keys(); colors.hasNext(); ) {
                var colorKey = colors.next();
                int color = hexStringToColorInt(jsonRawColors.getString(colorKey));
                RESOURCE_COLORS.put(colorKey.toLowerCase(), color);
            }

            for (var colors = jsonSemanticColors.keys(); colors.hasNext(); ) {
                var componentName = colors.next();
                var componentColors = jsonSemanticColors.getJSONArray(componentName);

                int[] value;
                if (componentColors.length() == 1) {
                    value = new int[]{hexStringToColorInt(componentColors.getString(0))};
                } else {
                    value = new int[]{
                            hexStringToColorInt(componentColors.getString(0)),
                            hexStringToColorInt(componentColors.getString(1))
                    };
                }

                COMPONENT_COLORS.put(componentName, value);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public int readRawColor(Object contextOrResource, int id, int originalColor) {
        waitUntilInitialized();

        Resources resources;
        if (contextOrResource instanceof Context) {
            resources = ((Context) contextOrResource).getResources();
        } else {
            resources = (Resources) contextOrResource;
        }

        var name = resources.getResourceEntryName(id);
        var color = RESOURCE_COLORS.get(name);

        return Objects.requireNonNullElse(color, originalColor);
    }
}
