package app.revanced.integrations.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.videoplayer.Fenster.FensterGestureController;
import app.revanced.integrations.videoplayer.Fenster.FensterGestureListener;
import app.revanced.integrations.videoplayer.Fenster.Helpers.BrightnessHelper;
import app.revanced.integrations.videoplayer.Fenster.XFenster;
import app.revanced.integrations.theme.XSettingActivity;
import app.revanced.integrations.utils.SwipeHelper;
import app.revanced.integrations.sponsorblock.NewSegmentHelperLayout;

/* loaded from: classes6.dex */
public class Settings {
    private static Object AutoRepeatClass;
    private static PlayerType env;
    private static FensterGestureController fensterGestureController;
    protected static Boolean debug = false;
    protected static Boolean settingsInitialized = false;
    protected static String manufacturerOverride = null;
    protected static String modelOverride = null;
    protected static Boolean overrideCodec = false;
    protected static Integer prefResolutionWIFI = -2;
    protected static Integer prefResolutionMobile = -2;
    protected static Float prefVideoSpeed = -2.0f;
    protected static Boolean prefAutoCaptions = false;
    protected static Boolean homeAdsShown = false;
    protected static Boolean videoAdsShown = false;
    protected static Boolean reelShown = false;
    protected static Boolean suggestionsShown = true;
    protected static Boolean infoCardsShown = true;
    protected static Boolean brandingShown = true;
    protected static Boolean castButtonShown = false;
    protected static Boolean tabletMiniplayer = false;
    protected static Boolean commentsLocation = false;
    protected static Boolean newActionBar = false;
    protected static Boolean verticalZoomToFit = false;
    protected static Boolean isDarkApp = false;
    protected static Boolean accessibilitySeek = false;
    protected static Boolean HDRBrightness = true;
    protected static Boolean EnableXFensterBrightness = false;
    protected static Boolean EnableXFensterVolume = false;
    protected static Integer maxBuffer = 120000;
    protected static Integer playbackMS = 2500;
    protected static Integer reBuffer = 5000;

    public static Boolean userChangedQuality = false;
    public static Boolean userChangedSpeed = false;
    public static Boolean newVideo = false;
    public static Boolean newVideoSpeed = false;
    public static Boolean XFILEDEBUG = false;

    private static void ReadSettings() {
        Context context;
        if (!settingsInitialized.booleanValue() && (context = YouTubeTikTokRoot_Application.getAppContext()) != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            debug = Boolean.valueOf(sharedPreferences.getBoolean("debug_xfile_enabled", false));
            manufacturerOverride = sharedPreferences.getString("override_manufacturer", null);
            modelOverride = sharedPreferences.getString("override_model", null);
            overrideCodec = sharedPreferences.getBoolean("override_resolution_xfile_enabled", false);
            prefResolutionWIFI = Integer.parseInt(sharedPreferences.getString("pref_preferred_video_quality_wifi", "-2"));
            prefResolutionMobile = Integer.valueOf(Integer.parseInt(sharedPreferences.getString("pref_preferred_video_quality_mobile", "-2")));
            prefVideoSpeed = Float.valueOf(Float.parseFloat(sharedPreferences.getString("pref_preferred_video_speed", "-2")));
            prefAutoCaptions = Boolean.valueOf(sharedPreferences.getBoolean("pref_auto_captions", false));
            homeAdsShown = Boolean.valueOf(sharedPreferences.getBoolean("home_ads_enabled", false));
            videoAdsShown = Boolean.valueOf(sharedPreferences.getBoolean("video_ads_enabled", false));
            reelShown = Boolean.valueOf(sharedPreferences.getBoolean("reel_enabled", false));
            suggestionsShown = Boolean.valueOf(sharedPreferences.getBoolean("info_card_suggestions_enabled", true));
            infoCardsShown = Boolean.valueOf(sharedPreferences.getBoolean("info_cards_enabled", true));
            brandingShown = Boolean.valueOf(sharedPreferences.getBoolean("branding_watermark_enabled", true));
            castButtonShown = Boolean.valueOf(sharedPreferences.getBoolean("cast_button_enabled", false));
            tabletMiniplayer = Boolean.valueOf(sharedPreferences.getBoolean("tablet_miniplayer", false));
            commentsLocation = Boolean.valueOf(sharedPreferences.getBoolean("comments_location", false));
            newActionBar = Boolean.valueOf(sharedPreferences.getBoolean("xfile_new_actionbar", false));
            verticalZoomToFit = Boolean.valueOf(sharedPreferences.getBoolean("xfile_zoom_to_fit_vertical", false));
            isDarkApp = Boolean.valueOf(sharedPreferences.getBoolean("app_theme_dark", false));
            accessibilitySeek = Boolean.valueOf(sharedPreferences.getBoolean("xfile_accessibility_seek_buttons", false));
            HDRBrightness = Boolean.valueOf(sharedPreferences.getBoolean("pref_hdr_autobrightness", true));
            if (sharedPreferences.getBoolean("pref_xfenster", false)) {
                sharedPreferences.edit().remove("pref_xfenster").putBoolean("pref_xfenster_brightness", true).putBoolean("pref_xfenster_volume", true).apply();
            }
            EnableXFensterBrightness = Boolean.valueOf(sharedPreferences.getBoolean("pref_xfenster_brightness", false));
            EnableXFensterVolume = Boolean.valueOf(sharedPreferences.getBoolean("pref_xfenster_volume", false));
            try {
                FensterGestureListener.SWIPE_THRESHOLD = Integer.parseInt(sharedPreferences.getString("pref_xfenster_swipe_threshold", "0"));
            } catch (NumberFormatException e) {
                sharedPreferences.edit().putString("pref_xfenster_swipe_threshold", "0").apply();
                FensterGestureListener.SWIPE_THRESHOLD = 0;
            }
            try {
                FensterGestureListener.TOP_PADDING = Integer.parseInt(sharedPreferences.getString("pref_xfenster_swipe_padding_top", "20"));
            } catch (NumberFormatException e2) {
                sharedPreferences.edit().putString("pref_xfenster_swipe_padding_top", "20").apply();
                FensterGestureListener.TOP_PADDING = 20;
            }
            String string = sharedPreferences.getString("pref_max_buffer_ms", "120000");
            if (string.isEmpty()) {
                string = "1";
            }
            maxBuffer = Integer.valueOf(Integer.parseInt(string));
            String string2 = sharedPreferences.getString("pref_buffer_for_playback_ms", "2500");
            if (string2.isEmpty()) {
                string2 = "1";
            }
            playbackMS = Integer.valueOf(Integer.parseInt(string2));
            String string3 = sharedPreferences.getString("pref_buffer_for_playback_after_rebuffer_ms", "5000");
            if (string3.isEmpty()) {
                string3 = "1";
            }
            reBuffer = Integer.valueOf(Integer.parseInt(string3));
            settingsInitialized = true;
        }
    }

    /**
     *  Checks if debug log has been enabled in the Settings
     * @return false as default value
     */
    public static boolean isDebug() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning false for debug!");
            return false;
        }
        return debug;
    }

    /**
     * Getter for prefResolutionMobile
     * @return Integer
     */
    public static Integer getPreferredMobileResolution() {
        return prefResolutionMobile;
    }

    public static Integer getPreferredWifiResolution() {
        return prefResolutionWIFI;
    }

    public static Float getPreferredVideoSpeed() {
        return prefVideoSpeed;
    }

    public static boolean isSuggestionsShown() {
        ReadSettings();
        String message = suggestionsShown ? "Suggestions: Shown" : "Suggestions: Hidden";
        LogHelper.debug("Settings", message);
        return suggestionsShown;
    }

    public static boolean isInfoCardsShown()  {
        ReadSettings();
        String message = infoCardsShown ? "InfoCards: Shown" : "InfoCards: Hidden";
        LogHelper.debug("Settings", message);
        return infoCardsShown;
    }

    public static boolean isBrandingShown() {
        ReadSettings();
        String message = brandingShown ? "Branding: Shown" : "Branding: Hidden";
        LogHelper.debug("Settings", message);
        return brandingShown;
    }

    public static boolean isHomeAdsShown() {
        ReadSettings();
        String message = homeAdsShown ? "Homeads: Shown" : "Homeads: Hidden";
        LogHelper.debug("Settings", message);
        return homeAdsShown;
    }

    public static boolean isVideoAdsShown() {
        ReadSettings();
        String message = videoAdsShown ? "Videoads: Shown" : "Videoads: Hidden";
        LogHelper.debug("Settings", message);
        return videoAdsShown;
    }

    public static boolean isReelsShown() {
        ReadSettings();
        String message = reelShown ? "Reels: Shown" : "Reels: Hidden";
        LogHelper.debug("Settings", message);
        return reelShown;
    }

    public static boolean isCastButtonShown() {
        ReadSettings();
        String message = castButtonShown ? "Castbutton: Shown" : "Castbutton: Hidden";
        LogHelper.debug("Settings", message);
        return castButtonShown;
    }

    public static String getManufacturer() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning Build.MANUFACTURER!");
            return Build.MANUFACTURER;
        }
        String manufacturer = manufacturerOverride;
        if (manufacturer == null || manufacturer.isEmpty()) {
            manufacturer = Build.MANUFACTURER;
        }
        LogHelper.debug("Settings", "getManufacturer: " + manufacturer);
        return manufacturer;
    }

    public static String getModel() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning Build.MODEL!");
            return Build.MODEL;
        }
        String model = modelOverride;
        if (model == null || model.isEmpty()) {
            model = Build.MODEL;
        }
        LogHelper.debug("Settings", "getModel: " + model);
        return model;
    }

    public static boolean autoCaptions(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean captions = Boolean.valueOf(original);
        if (prefAutoCaptions.booleanValue()) {
            captions = true;
        }
        LogHelper.debug("Settings", "autoCaptions: " + captions);
        return captions.booleanValue();
    }

    public static boolean getOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean compatibility = Boolean.valueOf(original);
        if (overrideCodec.booleanValue()) {
            compatibility = true;
        }
        LogHelper.debug("Settings", "getOverride: " + compatibility);

        return compatibility.booleanValue();
    }

    public static int getCommentsLocation(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!commentsLocation.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getCommentsLocation: Moving comments back down");
            return 3;
        }
    }

    public static boolean getTabletMiniplayerOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!tabletMiniplayer.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getTabletMiniplayerOverride: Using tablet miniplayer");
            return true;
        }
    }

    public static boolean getNewActionBar(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getNewActionBar: Enabled");
            return true;
        }
    }

    public static int getCastButtonOverrideV2(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (castButtonShown.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getCastButtonOverrideV2: Hidden by override");
            return 8;
        }
    }

    public static boolean getNewActionBarNegated(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getNewActionBar: Enabled");
            return false;
        }
    }

    public static boolean getVerticalZoomToFit(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!verticalZoomToFit.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getVerticalZoomToFit: Enabled");
            return true;
        }
    }

    public static int getMinimizedVideo(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int preferredType = Integer.parseInt(sharedPreferences.getString("pref_minimized_video_preview", "-2"));
        if (preferredType == -2) {
            return original;
        }
        if (preferredType == 0 || preferredType == 1) {
            return preferredType;
        }
        return original;
    }

    public static boolean getThemeStatus() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning false!");
            return false;
        } else if (!isDarkApp.booleanValue()) {
            return false;
        } else {
            LogHelper.debug("Settings", "getThemeStatus: Is themed");
            return true;
        }
    }

    public static boolean accessibilitySeek(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean seek = Boolean.valueOf(original);
        if (accessibilitySeek.booleanValue()) {
            seek = true;
        }
        LogHelper.debug("Settings", "accessibilitySeek: " + seek);
        return seek.booleanValue();
    }

    public static boolean useOldStyleQualitySettings() {
        boolean value;
        try {
            Context context = YouTubeTikTokRoot_Application.getAppContext();
            if (context == null) {
                LogHelper.printException("Settings", "useOldStyleQualitySettings - Context is null, returning false!");
                value = true;
            } else {
                SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
                value = sharedPreferences.getBoolean("old_style_quality_settings", true);
                LogHelper.debug("Settings", "old_style_quality_settings set to: " + value);
            }
            return value;
        } catch (Exception ex) {
            LogHelper.printException("Settings", "Unable to get old style quality settings", ex);
            return true;
        }
    }

    public static boolean shouldAutoRepeat() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "shouldAutoRepeat - Context is null, returning false!");
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        boolean repeat = sharedPreferences.getBoolean("pref_auto_repeat", false);
        LogHelper.debug("Settings", "shouldAutoRepeat: " + repeat);
        return repeat;
    }

    @Deprecated
    public static void trySetAutonav(boolean autoNav) {
        try {
            ReadSettings();
            Context context = YouTubeTikTokRoot_Application.getAppContext();
            if (context == null) {
                LogHelper.printException("Settings", "shouldAutoRepeat - Context is null, returning false!");
                return;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            sharedPreferences.edit().putBoolean("autonav_settings_activity_key", autoNav).apply();
            LogHelper.debug("Settings", "autonav_settings_activity_key set to: " + autoNav);
        } catch (Exception e) {
        }
    }

    public static float getHDRBrightness(float original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, getHDRBrightness returning " + original + "!");
            return original;
        }
        float finalValue = original;
        if (!HDRBrightness.booleanValue()) {
            if (isFensterBrightnessEnabled()) {
                finalValue = BrightnessHelper.getBrightness();
            } else {
                finalValue = -1.0f;
            }
            LogHelper.debug("Settings", "getHDRBrightness switched to: " + finalValue);

        }
        LogHelper.debug("Settings", "getHDRBrightness: " + finalValue);

        return finalValue;
    }

    public static int getMaxBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = maxBuffer.intValue();
        return retrievedValue;
    }

    public static int getPlaybackBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = playbackMS.intValue();
        LogHelper.debug("Settings", "getPlaybackBuffer switched to: " + retrievedValue);

        return retrievedValue;
    }

    public static int getReBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = reBuffer.intValue();
        LogHelper.debug("Settings", "getReBuffer switched to: " + retrievedValue);

        return retrievedValue;
    }

    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        fensterGestureController = new FensterGestureController();
        fensterGestureController.setFensterEventsListener(new XFenster(context, viewGroup), context, viewConfiguration);
        LogHelper.debug("Settings", "XFenster initialized");
    }

    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        if (fensterGestureController == null) {
            LogHelper.debug("Settings", "fensterGestureController is null");
            return false;
        } else if (motionEvent == null) {
            LogHelper.debug("Settings", "motionEvent is null");
            return false;
        } else if (!SwipeHelper.IsControlsShown()) {
            return fensterGestureController.onTouchEvent(motionEvent);
        } else {
            LogHelper.debug("Settings", "skipping onTouchEvent dispatching because controls are shown.");
            return false;
        }
    }

    public static void PlayerTypeChanged(PlayerType playerType) {
        LogHelper.debug("XDebug", playerType.toString());
        if (env != playerType) {
            String playerTypeString = playerType.toString();
            if (playerTypeString.equals("WATCH_WHILE_FULLSCREEN")) {
                EnableXFenster();
            } else {
                DisableXFenster();
            }
            if (playerTypeString.equals("WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED") || playerTypeString.equals("WATCH_WHILE_MINIMIZED") || playerTypeString.equals("WATCH_WHILE_PICTURE_IN_PICTURE")) {
                NewSegmentHelperLayout.hide();
            }
            app.revanced.integrations.sponsorblock.player.PlayerType.playerTypeChanged(playerTypeString);
        }
        env = playerType;
    }

    public static void EnableXFenster() {
        if (EnableXFensterBrightness.booleanValue() || EnableXFensterVolume.booleanValue()) {
            FensterGestureController fensterGestureController2 = fensterGestureController;
            fensterGestureController2.TouchesEnabled = true;
            ((XFenster) fensterGestureController2.listener).enable(EnableXFensterBrightness.booleanValue(), EnableXFensterVolume.booleanValue());
        }
    }

    public static void DisableXFenster() {
        FensterGestureController fensterGestureController2 = fensterGestureController;
        fensterGestureController2.TouchesEnabled = false;
        ((XFenster) fensterGestureController2.listener).disable();
    }

    public static boolean isFensterBrightnessEnabled() {
        return EnableXFensterBrightness.booleanValue();
    }

    public static void CheckForMicroG(Activity activity) {
        AlertDialog.Builder builder;
        if (!appInstalledOrNot("com.mgoogle.android.gms")) {
            LogHelper.debug("XDebug", "Custom MicroG installation undetected");
            if (Build.VERSION.SDK_INT >= 21) {
                builder = new AlertDialog.Builder(activity, 16974374);
            } else {
                builder = new AlertDialog.Builder(activity);
            }
            builder.setTitle("Someone is not reading...").setMessage("You didn't install the MicroG as instructed, you can't login without it.\n\nInstall it and try again.").setPositiveButton("Close", new DialogInterface.OnClickListener() { // from class: app.revanced.integrations.settings.Settings.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).show();
        } else {
            LogHelper.debug("XDebug", "Custom MicroG installation detected");
        }
    }

    public static boolean isFensterEnabled() {
        if (env != null && env.toString().equals("WATCH_WHILE_FULLSCREEN") && !SwipeHelper.IsControlsShown()) {
            return EnableXFensterBrightness.booleanValue() || EnableXFensterVolume.booleanValue();
        }
        return false;
    }

    public static boolean isWatchWhileFullScreen() {
        if (env == null) {
            return false;
        }
        return env.toString().equals("WATCH_WHILE_FULLSCREEN");
    }

    private static boolean appInstalledOrNot(String uri) {
        try {
            PackageManager pm = getContext().getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static String getVersionName() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private static int appGetFirstTimeRun() {
        SharedPreferences appPreferences = getContext().getSharedPreferences("youtube_revanced", 0);
        String appCurrentBuildVersion = getVersionName();
        String appLastBuildVersion = appPreferences.getString("app_first_time", null);
        if (appLastBuildVersion == null || !appLastBuildVersion.equalsIgnoreCase(appCurrentBuildVersion)) {
            return appLastBuildVersion == null ? 0 : 2;
        }
        return 1;
    }

    public static String getPackageName() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (XFILEDEBUG.booleanValue() && context == null) {
            context = XSettingActivity.getAppContext();
        }
        if (context == null) {
            LogHelper.printException("Settings", "Context is null, returning com.google.android.youtube!");
            return "com.google.android.youtube";
        }
        String PACKAGE_NAME = context.getPackageName();
        LogHelper.debug("Settings", "getPackageName: " + PACKAGE_NAME);

        return PACKAGE_NAME;
    }

    public static String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            LogHelper.printException("Settings", "Resource not found.", exception);
            return "";
        }
    }

    public static int getOverrideWidth(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int compatibility = original;
        if (sharedPreferences.getBoolean("override_resolution_xfile_enabled", false)) {
            compatibility = 2160;
        }
        LogHelper.debug("Settings", "getOverrideWidth: " + compatibility);
        return compatibility;
    }

    public static int getOverrideHeight(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int compatibility = original;
        if (sharedPreferences.getBoolean("override_resolution_xfile_enabled", false)) {
            compatibility = 3840;
        }
        LogHelper.debug("Settings", "getOverrideHeight: " + compatibility);

        return compatibility;
    }

    public static Context getContext() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context != null) {
            LogHelper.debug("Settings", "getContext");
            return context;
        } else if (XFILEDEBUG.booleanValue()) {
            return XSettingActivity.getAppContext();
        } else {
            LogHelper.printException("Settings", "Context is null, returning null!");
            return null;
        }
    }

    public static void setOldLayout(SharedPreferences sharedPreferences, String config, long timeStamp) {
        ReadSettings();
        if (!sharedPreferences.getBoolean("old_layout_xfile_enabled", false)) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group", config).putLong("com.google.android.libraries.youtube.innertube.cold_stored_timestamp", timeStamp).apply();
            LogHelper.debug("Settings", "setOldLayout: true");
            return;
        }
        if (sharedPreferences.contains("com.google.android.libraries.youtube.innertube.cold_config_group")) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group_backup", sharedPreferences.getString("com.google.android.libraries.youtube.innertube.cold_config_group", null)).remove("com.google.android.libraries.youtube.innertube.cold_config_group").apply();
        }
        LogHelper.debug("Settings", "setOldLayout: false");
    }

    public static void NewVideoStarted() {
        ReadSettings();
        newVideo = true;
        newVideoSpeed = true;
        LogHelper.debug("Settings", "New video started!");
    }

    public static boolean ExecuteShellCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            in.close();
            LogHelper.debug("XDebug", "Command Output: " + line);
            if (line.contains("m0yP")) {
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Exception e) {
                    }
                }
                return true;
            }
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e2) {
                }
            }
            return false;
        } catch (Exception e3) {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e4) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e5) {
                }
            }
            throw th;
        }
    }

    public static Object PrintSettings(Object[] settings) {
        Exception e;
        Exception e2;
        Class<?> stringType;
        ReadSettings();
        Class<?> stringType2 = String.class;
        LogHelper.debug("Protobuf", "new settings array");
        Object[] newArray = new Object[settings.length + 1];
        boolean found = false;
        int index = 0;
        try {
            LogHelper.debug("Protobuf", "Printing array");
            try {
                try {
                    int i = 0;
                    String className = null;
                    for (Object settingObject : settings) {
                        try {
                            Field[] fields = settingObject.getClass().getFields();
                            int length = fields.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                }
                                Field field = fields[i2];
                                if (field.getType().isAssignableFrom(stringType2) && field.getName().equals("a")) {
                                    String value = field.get(settingObject).toString();
                                    if (value.equals("e")) {
                                        className = settingObject.getClass().getName();
                                        found = true;
                                        break;
                                    }
                                }
                                i2++;
                            }
                            index++;
                            if (found) {
                                break;
                            }
                        } catch (Exception e4) {
                            e2 = e4;
                            LogHelper.printException("Protobuf", "Error: " + e2.getMessage());
                            return settings;
                        }
                    }
                    if (found) {
                        LogHelper.debug("Protobuf", "Modifying array");
                        System.arraycopy(settings, 0, newArray, 0, index - 1);
                        Class<?> clazz = Class.forName(className);
                        Object object = clazz.newInstance();
                        newArray[index - 1] = object;
                        System.arraycopy(settings, index - 1, newArray, index, (settings.length - index) + 1);
                    } else {
                        newArray = settings;
                    }
                    int length2 = newArray.length;
                    boolean hitE = false;
                    int loop = 0;
                    int loop2 = 0;
                    while (loop2 < length2) {
                        Object settingObject2 = newArray[loop2];
                        Field[] fields2 = settingObject2.getClass().getFields();
                        int length3 = fields2.length;
                        boolean hitE2 = hitE;
                        while (i < length3) {
                            Field field2 = fields2[i];
                            if (!field2.getType().isAssignableFrom(stringType2)) {
                                stringType = stringType2;
                                length2 = length2;
                            } else if (field2.getName().equals("a")) {
                                if (loop != index - 1 || !found) {
                                    stringType = stringType2;
                                } else {
                                    if (isDebug()) {
                                        StringBuilder sb = new StringBuilder();
                                        stringType = stringType2;
                                        sb.append("String a field: ");
                                        sb.append(field2.get(settingObject2).toString());
                                        sb.append(" set: country-type");
                                        LogHelper.debug("Protobuf", sb.toString());
                                    } else {
                                        stringType = stringType2;
                                    }
                                    field2.set(settingObject2, "country-type");
                                }
                                String value2 = field2.get(settingObject2).toString();
                                if (value2.equals("e")) {
                                    hitE2 = true;
                                }
                                if (isDebug()) {
                                    StringBuilder sb2 = new StringBuilder();
                                    length2 = length2;
                                    sb2.append("String a field: ");
                                    sb2.append(value2);
                                    LogHelper.debug("Protobuf", sb2.toString());
                                } else {
                                    length2 = length2;
                                }
                            } else {
                                stringType = stringType2;
                                length2 = length2;
                                if (field2.getName().equals("b")) {
                                    if (loop == index - 1 && found) {
                                        LogHelper.debug("Protobuf", "String b field: " + field2.get(settingObject2).toString() + " set: B");
                                        field2.set(settingObject2, "B");
                                    }
                                    String value3 = field2.get(settingObject2).toString();
                                    if (hitE2) {
                                        field2.set(settingObject2, "11202606,9415293,9422596,9429003,9431755,9435797,9442923,9444108,9444635,9449243,9453077,9456940,9463829,9464088,9467503,9476327,9477614,9478523,9480475,9480495,9482942,9483422,9483531,9484706,9485998,9487653,9487664,9488038,9488230,9489113");
                                        hitE2 = false;
                                    }
                                    LogHelper.debug("Protobuf", "String b field: " + value3);
                                } else {
                                    String value4 = field2.get(settingObject2).toString();
                                    LogHelper.debug("Protobuf", "String field: " + field2.getName() + " = " + value4);
                                }
                            }
                            i++;
                            stringType2 = stringType;
                        }
                        loop++;
                        loop2++;
                        hitE = hitE2;
                        i = 0;
                    }
                    return newArray;
                } catch (Exception e5) {
                    e = e5;
                    LogHelper.printException("Protobuf", "Error: " + e.getMessage());
                    return settings;
                }
            } catch (Exception e6) {
                e2 = e6;
            }
        } catch (Exception e7) {
            e = e7;
        }
        return null;
    }

    public static Object PrintSettings(Object[] settings, int type) {
        Exception e;
        int i = 0;
        boolean found;
        Class<?> stringType;
        ReadSettings();
        boolean modifyArray = type == 2;
        Class<?> stringType2 = String.class;
        LogHelper.debug("Protobuf", "new settings array");
        Object[] newArray = new Object[settings.length + 1];
        if (!modifyArray) {
            newArray = settings;
        }
        boolean found2 = false;
        int index = 0;
        if (modifyArray) {
            try {
                LogHelper.debug("Protobuf", "Modifying array");
                try {
                    int length = settings.length;
                    int index2 = 0;
                    boolean found3 = false;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            found2 = found3;
                            index = index2;
                            break;
                        }
                        try {
                            Object settingObject = settings[i2];
                            Field[] fields = settingObject.getClass().getFields();
                            int length2 = fields.length;
                            int i3 = 0;
                            while (true) {
                                if (i3 >= length2) {
                                    break;
                                }
                                Field field = fields[i3];
                                if (field.getType().isAssignableFrom(stringType2) && field.getName().equals("a")) {
                                    String value = field.get(settingObject).toString();
                                    if (value.equals("e")) {
                                        found3 = true;
                                        break;
                                    }
                                }
                                i3++;
                            }
                            index2++;
                            if (found3) {
                                found2 = found3;
                                index = index2;
                                break;
                            }
                            i2++;
                        } catch (Exception e2) {
                            e = e2;
                            LogHelper.printException("Protobuf", "Error: " + e.getMessage());
                            return settings;
                        }
                    }
                    i = 0;
                    System.arraycopy(settings, 0, newArray, 0, index);
                    System.arraycopy(settings, index - 1, newArray, index, (settings.length - index) + 1);
                } catch (Exception e3) {
                    e = e3;
                }
            } catch (Exception e4) {
                e = e4;
                LogHelper.printException("Protobuf", "Error: " + e.getMessage());
                return settings;
            }
        } else {
            i = 0;
            newArray = settings;
        }
        try {
            int length3 = newArray.length;
            int loop = 0;
            int loop2 = i;
            while (loop2 < length3) {
                Object settingObject2 = newArray[loop2];
                Field[] fields2 = settingObject2.getClass().getFields();
                int length4 = fields2.length;
                while (i < length4) {
                    Field field2 = fields2[i];
                    if (!field2.getType().isAssignableFrom(stringType2)) {
                        stringType = stringType2;
                        found = found2;
                    } else if (field2.getName().equals("a")) {
                        if (loop == index - 1 && modifyArray) {
                            field2.set(settingObject2, "country-type");
                        }
                        String value2 = field2.get(settingObject2).toString();
                        if (isDebug()) {
                            stringType = stringType2;
                            try {
                                StringBuilder sb = new StringBuilder();
                                found = found2;
                                sb.append("String a field: ");
                                sb.append(value2);
                                LogHelper.debug("Protobuf", sb.toString());
                            } catch (Exception e5) {
                                e = e5;
                                LogHelper.printException("Protobuf", "Error: " + e.getMessage());
                                return settings;
                            }
                        } else {
                            stringType = stringType2;
                            found = found2;
                        }
                    } else {
                        stringType = stringType2;
                        found = found2;
                        if (field2.getName().equals("b")) {
                            if (loop == index - 1 && modifyArray) {
                                field2.set(settingObject2, "B");
                            }
                            String value3 = field2.get(settingObject2).toString();
                            LogHelper.debug("Protobuf", "String b field: " + value3);
                        } else {
                            String value4 = field2.get(settingObject2).toString();
                            LogHelper.debug("Protobuf", "String field: " + field2.getName() + " = " + value4);
                        }
                    }
                    i++;
                    stringType2 = stringType;
                    found2 = found;
                }
                loop++;
                loop2++;
                i = 0;
            }
            return newArray;
        } catch (Exception e6) {
            e = e6;
        }
        return null;
    }

    public static void PrintVideoQualities(Object quality, int mode) {
        Class<?> intType;
        Class<?> stringType;
        Field fieldArray;
        ReadSettings();
        LogHelper.debug("VideoQualities", "Quality parameter: " + mode);
        if (mode == 0) {
            Class<?> intType2 = Integer.TYPE;
            Class<?> stringType2 = String.class;
            Class<?> boolType = Boolean.TYPE;
            try {
                Class<?> clazz = quality.getClass();
                Field fieldArray2 = clazz.getField("e");
                Object[] fieldValue = (Object[]) fieldArray2.get(quality);
                ArrayList<Integer> iStreamQualities = new ArrayList<>();
                ArrayList<String> sStreamQualities = new ArrayList<>();
                ArrayList<Boolean> bStreamQualities = new ArrayList<>();
                int length = fieldValue.length;
                int i = 0;
                while (i < length) {
                    Object streamQuality = fieldValue[i];
                    Field[] fields = streamQuality.getClass().getFields();
                    int length2 = fields.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        Field field = fields[i2];
                        if (field.getType().isAssignableFrom(intType2)) {
                            try {
                                int value = field.getInt(streamQuality);
                                intType = intType2;
                                try {
                                    int length3 = field.getName().length();
                                    fieldArray = fieldArray2;
                                    if (length3 <= 2) {
                                        iStreamQualities.add(Integer.valueOf(value));
                                    }
                                    LogHelper.debug("VideoQualities", "Integer field: " + field.getName() + " = " + value);
                                    stringType = stringType2;
                                } catch (Exception e) {
                                    return;
                                }
                            } catch (Exception e2) {
                                return;
                            }
                        } else {
                            intType = intType2;
                            fieldArray = fieldArray2;
                            try {
                                if (field.getType().isAssignableFrom(stringType2)) {
                                    String value2 = field.get(streamQuality).toString();
                                    sStreamQualities.add(value2);
                                    if (isDebug()) {
                                        StringBuilder sb = new StringBuilder();
                                        stringType = stringType2;
                                        try {
                                            sb.append("String field: ");
                                            sb.append(field.getName());
                                            sb.append(" = ");
                                            sb.append(value2);
                                            LogHelper.debug("VideoQualities", sb.toString());
                                        } catch (Exception e3) {
                                            return;
                                        }
                                    } else {
                                        stringType = stringType2;
                                    }
                                } else {
                                    stringType = stringType2;
                                    if (field.getType().isAssignableFrom(boolType)) {
                                        boolean value3 = field.getBoolean(streamQuality);
                                        bStreamQualities.add(Boolean.valueOf(value3));
                                        LogHelper.debug("VideoQualities", "Boolean field: " + field.getName() + " = " + value3);
                                    }
                                }
                            } catch (Exception e4) {
                                return;
                            }
                        }
                        i2++;
                        fieldArray2 = fieldArray;
                        stringType2 = stringType;
                        intType2 = intType;
                    }
                    i++;
                    clazz = clazz;
                }
            } catch (Exception e5) {
            }
        }
    }

    public static void PrintQualities(Object[] qualities, int quality) {
        ArrayList<Integer> iStreamQualities;
        Class<?> intType;
        Object[] objArr = qualities;
        ReadSettings();
        Class<?> intType2 = Integer.TYPE;
        Class<?> boolType = Boolean.TYPE;
        LogHelper.debug("QUALITY", "Quality parameter: " + quality);
        try {
            ArrayList<Integer> iStreamQualities2 = new ArrayList<>();
            ArrayList<String> sStreamQualities = new ArrayList<>();
            ArrayList<Boolean> bStreamQualities = new ArrayList<>();
            int length = objArr.length;
            int i = 0;
            while (i < length) {
                Object streamQuality = objArr[i];
                Field[] fields = streamQuality.getClass().getFields();
                int length2 = fields.length;
                int i2 = 0;
                while (i2 < length2) {
                    Field field = fields[i2];
                    if (field.getType().isAssignableFrom(intType2)) {
                        int value = field.getInt(streamQuality);
                        intType = intType2;
                        if (field.getName().length() <= 2) {
                            try {
                                iStreamQualities2.add(Integer.valueOf(value));
                            } catch (Exception e) {
                                return;
                            }
                        }
                        if (isDebug()) {
                            StringBuilder sb = new StringBuilder();
                            iStreamQualities = iStreamQualities2;
                            sb.append("Integer field: ");
                            sb.append(field.getName());
                            sb.append(" = ");
                            sb.append(value);
                            LogHelper.debug("QUALITY", sb.toString());
                        } else {
                            iStreamQualities = iStreamQualities2;
                        }
                    } else {
                        iStreamQualities = iStreamQualities2;
                        intType = intType2;
                        if (field.getType().isAssignableFrom(String.class)) {
                            String value2 = field.get(streamQuality).toString();
                            sStreamQualities.add(value2);
                            LogHelper.debug("QUALITY", "String field: " + field.getName() + " = " + value2);
                        } else if (field.getType().isAssignableFrom(boolType)) {
                            boolean value3 = field.getBoolean(streamQuality);
                            bStreamQualities.add(Boolean.valueOf(value3));
                            LogHelper.debug("QUALITY", "Boolean field: " + field.getName() + " = " + value3);
                        }
                    }
                    i2++;
                    intType2 = intType;
                    iStreamQualities2 = iStreamQualities;
                }
                i++;
                objArr = qualities;
            }
        } catch (Exception e2) {
        }
    }

    public static ColorStateList getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            LogHelper.printException("Settings", "Not found color resource by id: " + colorRes);
        }
        int[][] states = {new int[]{16842910}, new int[]{-16842910}, new int[]{-16842912}, new int[]{16842919}};
        int[] colors = {color, color, color, color};
        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }
}
