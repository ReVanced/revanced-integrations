package app.revanced.twitch.settings;

import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;
import app.revanced.twitch.utils.SharedPrefHelper;

public enum SettingsEnum {
    /* Ads */
    BLOCK_VIDEO_ADS("revanced_block_video_ads", true, ReturnType.BOOLEAN),
    BLOCK_AUDIO_ADS("revanced_block_audio_ads", true, ReturnType.BOOLEAN),

    /* Chat */
    SHOW_DELETED_MESSAGES("revanced_show_deleted_messages", "cross-out", ReturnType.STRING),

    /* Misc */
    DEBUG_MODE("revanced_debug_mode", false, ReturnType.BOOLEAN, true);

    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;

    private Object value = null;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.REVANCED_PREFS;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, Boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.REVANCED_PREFS;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    static {
        load();
    }

    private static void load() {
        ReVancedUtils.ifContextAttached((context -> {
            try {
                for (SettingsEnum setting : values()) {
                    Object value = setting.getDefaultValue();

                    switch (setting.getReturnType()) {
                        case FLOAT:
                            value = SharedPrefHelper.getFloat(context, setting.sharedPref, setting.getPath(), (float) setting.getDefaultValue());
                            break;
                        case LONG:
                            value = SharedPrefHelper.getLong(context, setting.sharedPref, setting.getPath(), (long) setting.getDefaultValue());
                            break;
                        case BOOLEAN:
                            value = SharedPrefHelper.getBoolean(context, setting.sharedPref, setting.getPath(), (boolean) setting.getDefaultValue());
                            break;
                        case INTEGER:
                            value = SharedPrefHelper.getInt(context, setting.sharedPref, setting.getPath(), (int) setting.getDefaultValue());
                            break;
                        case STRING:
                            value = SharedPrefHelper.getString(context, setting.sharedPref, setting.getPath(), (String) setting.getDefaultValue());
                            break;
                        default:
                            LogHelper.error("Setting '%s' does not have a valid type", setting.name());
                            break;
                    }
                    setting.setValue(value);

                    LogHelper.debug("Loaded setting '%s' with value %s", setting.name(), value);
                }
            } catch (Throwable th) {
                LogHelper.printException("Failed to load settings", th);
            }
        }));
    }

    public void setValue(Object newValue) {
        this.value = newValue;
    }

    public void saveValue(Object newValue) {
        ReVancedUtils.ifContextAttached((context) -> {
            if (returnType == ReturnType.BOOLEAN) {
                SharedPrefHelper.saveBoolean(context, sharedPref, path, (Boolean) newValue);
            } else {
                SharedPrefHelper.saveString(context, sharedPref, path, newValue + "");
            }
            value = newValue;
        });
    }

    public int getInt() {
        return (int) value;
    }

    public String getString() {
        return (String) value;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public Float getFloat() {
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public boolean shouldRebootOnChange() {
        return rebootApp;
    }
}
