package app.revanced.integrations.videoplayer.videosettings;


import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.settings.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes6.dex */
public class VideoSpeed {
    static final float[] videoSpeeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

    public static int DefaultSpeed(Object[] speeds, int speed, Object qInterface) {
        int speed2;
        Exception e;
        if (!Settings.newVideoSpeed) {
            return speed;
        }
        Settings.newVideoSpeed = false;
        LogHelper.debug("Settings - speeds", "Speed: " + speed);
        float preferredSpeed = Settings.getPreferredVideoSpeed();
        LogHelper.debug("Settings", "Preferred speed: " + preferredSpeed);
        if (preferredSpeed == -2.0f) {
            return speed;
        }
        Class<?> floatType = Float.TYPE;
        ArrayList<Float> iStreamSpeeds = new ArrayList<>();
        try {
            for (Object streamSpeed : speeds) {
                Field[] fields = streamSpeed.getClass().getFields();
                for (Field field : fields) {
                    if (field.getType().isAssignableFrom(floatType)) {
                        float value = field.getFloat(streamSpeed);
                        if (field.getName().length() <= 2) {
                            iStreamSpeeds.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Iterator<Float> it = iStreamSpeeds.iterator();
        int index = 0;
        while (it.hasNext()) {
            float streamSpeed2 = it.next();
            LogHelper.debug("Settings - speeds", "Speed at index " + index + ": " + streamSpeed2);
            index++;
        }
        int speed3 = -1;
        for (float streamSpeed3 : iStreamSpeeds) {
            if (streamSpeed3 <= preferredSpeed) {
                speed3++;
                LogHelper.debug("Settings - speeds", "Speed loop at index " + speed3 + ": " + streamSpeed3);
            }
        }
        if (speed3 == -1) {
            LogHelper.debug("Settings - speeds", "Speed was not found");
            speed2 = 3;
        } else {
            speed2 = speed3;
        }
        try {
            Method[] declaredMethods = qInterface.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getName().length() <= 2) {
                    LogHelper.debug("SPEED - Method", "Method name: " + method.getName());
                    try {
                        try {
                            method.invoke(qInterface, videoSpeeds[speed2]);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                        } catch (Exception e6) {
                            e = e6;
                            LogHelper.printException("XDebug", e.getMessage());
                            return speed2;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e10) {
            e = e10;
        }
        LogHelper.debug("Settings", "Speed changed to: " + speed2);
        return speed2;
    }

    public static void userChangedSpeed() {
        Settings.userChangedSpeed = true;
        Settings.newVideoSpeed = false;
    }

    private static float getSpeedByIndex(int index) {
        if (index == -2) {
            return 1.0f;
        }
        try {
            return videoSpeeds[index];
        } catch (Exception e) {
            return 1.0f;
        }
    }

    public static float getSpeedValue(Object[] speeds, int speed) {
        int i = 0;
        if (!Settings.newVideoSpeed || Settings.userChangedSpeed) {
            if (Settings.isDebug() && Settings.userChangedSpeed) {
                LogHelper.debug("Settings - speeds", "Skipping speed change because user changed it: " + speed);
            }
            Settings.userChangedSpeed = false;
            return -1.0f;
        }
        Settings.newVideoSpeed = false;
        LogHelper.debug("Settings - speeds", "Speed: " + speed);
        float preferredSpeed = Settings.getPreferredVideoSpeed();
        LogHelper.debug("Settings", "Preferred speed: " + preferredSpeed);
        if (preferredSpeed == -2.0f) {
            return -1.0f;
        }
        Class<?> floatType = Float.TYPE;
        ArrayList<Float> iStreamSpeeds = new ArrayList<>();
        try {
            int length = speeds.length;
            int i2 = 0;
            while (i2 < length) {
                Object streamSpeed = speeds[i2];
                Field[] fields = streamSpeed.getClass().getFields();
                int length2 = fields.length;
                while (i < length2) {
                    Field field = fields[i];
                    if (field.getType().isAssignableFrom(floatType)) {
                        float value = field.getFloat(streamSpeed);
                        if (field.getName().length() <= 2) {
                            iStreamSpeeds.add(value);
                        }
                    }
                    i++;
                }
                i2++;
                i = 0;
            }
        } catch (Exception ignored) {
        }
        int index = 0;
        for (Float iStreamSpeed : iStreamSpeeds) {
            float streamSpeed2 = iStreamSpeed;
            LogHelper.debug("Settings - speeds", "Speed at index " + index + ": " + streamSpeed2);
            index++;
        }
        int newSpeedIndex = -1;
        for (Float iStreamSpeed : iStreamSpeeds) {
            float streamSpeed3 = iStreamSpeed;
            if (streamSpeed3 <= preferredSpeed) {
                newSpeedIndex++;
                LogHelper.debug("Settings - speeds", "Speed loop at index " + newSpeedIndex + ": " + streamSpeed3);
            }
        }
        if (newSpeedIndex == -1) {
            LogHelper.debug("Settings - speeds", "Speed was not found");
            newSpeedIndex = 3;
        }
        if (newSpeedIndex == speed) {
            LogHelper.debug("Settings", "Trying to set speed to what it already is, skipping...: " + newSpeedIndex);
            return -1.0f;
        }
        LogHelper.debug("Settings", "Speed changed to: " + newSpeedIndex);
        return getSpeedByIndex(newSpeedIndex);
    }
}
