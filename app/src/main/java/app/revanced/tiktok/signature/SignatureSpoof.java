package app.revanced.tiktok.signature;

import android.content.Context;

import java.lang.reflect.Proxy;

public class SignatureSpoof {
    public static void enable(Context context) {
        Reflecter activityThread = Reflecter.on("android.app.ActivityThread");
        Reflecter sPackageManager = activityThread.field("sPackageManager");
        ProxyIPackageManager proxyIPackageManager = new ProxyIPackageManager(context, sPackageManager.get());
        Class<?> IPackageManagerClass = Reflecter.on("android.content.pm.IPackageManager").get();
        Object hookIPackageManager = Proxy.newProxyInstance(IPackageManagerClass.getClassLoader(), new Class[]{IPackageManagerClass}, proxyIPackageManager);
        activityThread.set("sPackageManager", hookIPackageManager);
        Reflecter packageManager = Reflecter.on(context.getPackageManager());
        packageManager.set("mPM", hookIPackageManager);
    }
}
