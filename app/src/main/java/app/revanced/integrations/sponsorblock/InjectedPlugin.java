package app.revanced.integrations.sponsorblock;


import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

// invoke-static {p0}, Lpl/jakubweg/InjectedPlugin;->inject(Landroid/content/Context;)V
// invoke-static {}, Lpl/jakubweg/InjectedPlugin;->printSomething()V
// InlineTimeBar
public class InjectedPlugin {

    private static final String TAG = "revanced.InjectedPlugin";

    public static void printSomething() {
        LogH(TAG, "printSomething called");
    }

    public static void printObject(Object o, int recursive) {
        if (o == null)
            LogH(TAG, "Printed object is null");
        else {
            LogH(TAG, "Printed object ("
                    + o.getClass().getName()
                    + ") = " + o.toString());
            for (Field field : o.getClass().getDeclaredFields()) {
                if (field.getType().isPrimitive())
                    continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(o);
                    try {
//                        if ("java.lang.String".equals(field.getType().getName()))
                        LogH(TAG, "Field: " + field.toString() + " has value " + value);
                    } catch (Exception e) {
                        LogH(TAG, "Field: " + field.toString() + " has value that thrown an exception in toString method");
                    }
                    if (recursive > 0 && value != null && !value.getClass().isPrimitive())
                        printObject(value, recursive - 1);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void printObject(Object o) {
        printObject(o, 0);
    }

    public static void printObject(int o) {
        printObject(Integer.valueOf(o));
    }

    public static void printObject(float o) {
        printObject(Float.valueOf(o));
    }

    public static void printObject(long o) {
        printObject(Long.valueOf(o));
    }

    public static void printStackTrace() {
        StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
        LogH(TAG, "Printing stack trace:");
        for (StackTraceElement element : stackTrace) {
            LogH(TAG, element.toString());
        }
    }

    public static void printViewStack(final View view, int spaces) {
        StringBuilder builder = new StringBuilder(spaces);
        for (int i = 0; i < spaces; i++) {
            builder.append('-');
        }
        String spacesStr = builder.toString();

        if (view == null) {
            Log.i(TAG, spacesStr + "Null view");
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            Log.i(TAG, spacesStr + "View group: " + view);
            int childCount = group.getChildCount();
            Log.i(TAG, spacesStr + "Children count: " + childCount);
            for (int i = 0; i < childCount; i++) {
                printViewStack(group.getChildAt(i), spaces + 1);
            }
        } else {
            Log.i(TAG, spacesStr + "Normal view: " + view);
        }
    }
}


