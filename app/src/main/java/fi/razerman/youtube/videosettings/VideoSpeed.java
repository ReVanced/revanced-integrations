package fi.razerman.youtube.videosettings;

import android.util.Log;
import fi.razerman.youtube.XGlobals;
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
        if (!XGlobals.newVideoSpeed.booleanValue()) {
            return speed;
        }
        XGlobals.newVideoSpeed = false;
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals - speeds", "Speed: " + speed);
        }
        float preferredSpeed = XGlobals.prefVideoSpeed.floatValue();
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals", "Preferred speed: " + preferredSpeed);
        }
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
                            iStreamSpeeds.add(Float.valueOf(value));
                        }
                    }
                }
            }
        } catch (Exception e2) {
        }
        Iterator<Float> it = iStreamSpeeds.iterator();
        int index = 0;
        while (it.hasNext()) {
            float streamSpeed2 = it.next().floatValue();
            if (XGlobals.debug.booleanValue()) {
                Log.d("XGlobals - speeds", "Speed at index " + index + ": " + streamSpeed2);
            }
            index++;
        }
        int speed3 = -1;
        Iterator<Float> it2 = iStreamSpeeds.iterator();
        while (it2.hasNext()) {
            float streamSpeed3 = it2.next().floatValue();
            if (streamSpeed3 <= preferredSpeed) {
                speed3++;
                if (XGlobals.debug.booleanValue()) {
                    Log.d("XGlobals - speeds", "Speed loop at index " + speed3 + ": " + streamSpeed3);
                }
            }
        }
        if (speed3 == -1) {
            if (XGlobals.debug.booleanValue()) {
                Log.d("XGlobals - speeds", "Speed was not found");
            }
            speed2 = 3;
        } else {
            speed2 = speed3;
        }
        try {
            Method[] declaredMethods = qInterface.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getName().length() <= 2) {
                    if (XGlobals.debug.booleanValue()) {
                        Log.d("SPEED - Method", "Method name: " + method.getName());
                    }
                    try {
                        try {
                            method.invoke(qInterface, Float.valueOf(videoSpeeds[speed2]));
                        } catch (IllegalAccessException e3) {
                        } catch (IllegalArgumentException e4) {
                        } catch (InvocationTargetException e5) {
                        } catch (Exception e6) {
                            e = e6;
                            Log.e("XDebug", e.getMessage());
                            if (XGlobals.debug.booleanValue()) {
                            }
                            return speed2;
                        }
                    } catch (Exception e8) {
                    }
                }
            }
        } catch (Exception e10) {
            e = e10;
        }
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals", "Speed changed to: " + speed2);
        }
        return speed2;
    }

    public static void userChangedSpeed() {
        XGlobals.userChangedSpeed = true;
        XGlobals.newVideoSpeed = false;
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
        if (!XGlobals.newVideoSpeed.booleanValue() || XGlobals.userChangedSpeed.booleanValue()) {
            if (XGlobals.debug.booleanValue() && XGlobals.userChangedSpeed.booleanValue()) {
                Log.d("XGlobals - speeds", "Skipping speed change because user changed it: " + speed);
            }
            XGlobals.userChangedSpeed = false;
            return -1.0f;
        }
        XGlobals.newVideoSpeed = false;
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals - speeds", "Speed: " + speed);
        }
        float preferredSpeed = XGlobals.prefVideoSpeed.floatValue();
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals", "Preferred speed: " + preferredSpeed);
        }
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
                            iStreamSpeeds.add(Float.valueOf(value));
                        }
                    }
                    i++;
                }
                i2++;
                i = 0;
            }
        } catch (Exception e) {
        }
        int index = 0;
        Iterator<Float> it = iStreamSpeeds.iterator();
        while (it.hasNext()) {
            float streamSpeed2 = it.next().floatValue();
            if (XGlobals.debug.booleanValue()) {
                Log.d("XGlobals - speeds", "Speed at index " + index + ": " + streamSpeed2);
            }
            index++;
        }
        int newSpeedIndex = -1;
        Iterator<Float> it2 = iStreamSpeeds.iterator();
        while (it2.hasNext()) {
            float streamSpeed3 = it2.next().floatValue();
            if (streamSpeed3 <= preferredSpeed) {
                newSpeedIndex++;
                if (XGlobals.debug.booleanValue()) {
                    Log.d("XGlobals - speeds", "Speed loop at index " + newSpeedIndex + ": " + streamSpeed3);
                }
            }
        }
        if (newSpeedIndex == -1) {
            if (XGlobals.debug.booleanValue()) {
                Log.d("XGlobals - speeds", "Speed was not found");
            }
            newSpeedIndex = 3;
        }
        if (newSpeedIndex == speed) {
            if (XGlobals.debug.booleanValue()) {
                Log.d("XGlobals", "Trying to set speed to what it already is, skipping...: " + newSpeedIndex);
            }
            return -1.0f;
        }
        if (XGlobals.debug.booleanValue()) {
            Log.d("XGlobals", "Speed changed to: " + newSpeedIndex);
        }
        return getSpeedByIndex(newSpeedIndex);
    }
}
