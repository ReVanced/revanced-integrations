package fi.razerman.youtube.videosettings;

import android.util.Log;
import fi.razerman.youtube.XGlobals;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes6.dex */
public class VideoSpeed {
    static final float[] videoSpeeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

    /* JADX WARN: Removed duplicated region for block: B:78:0x01ba  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static int DefaultSpeed(java.lang.Object[] r16, int r17, java.lang.Object r18) {
        /*
            Method dump skipped, instructions count: 465
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: fi.razerman.youtube.videosettings.VideoSpeed.DefaultSpeed(java.lang.Object[], int, java.lang.Object):int");
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
