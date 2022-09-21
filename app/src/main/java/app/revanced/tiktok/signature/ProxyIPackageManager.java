package app.revanced.tiktok.signature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.util.Base64;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyIPackageManager implements InvocationHandler {
    private final String packageName;
    private final Object original;
    private final Signature sign;

    public ProxyIPackageManager(Context context, Object original) {
        this.packageName = context.getPackageName();
        this.original = original;
        String originalSignature = "MIIDhzCCAm+gAwIBAgIEMsei9zANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwI4NjERMA8GA1UECBMIU2hhbmdoYWkxETAPBgNVBAcTCFNoYW5naGFpMRgwFgYDVQQKEw9tdXNpY2FsLmx5IEluYy4xEDAOBgNVBAsTB2FuZHJvaWQxEzARBgNVBAMTCm11c2ljYWwubHkwHhcNMTUwNDI4MDQyNzE3WhcNNDAwNDIxMDQyNzE3WjB0MQswCQYDVQQGEwI4NjERMA8GA1UECBMIU2hhbmdoYWkxETAPBgNVBAcTCFNoYW5naGFpMRgwFgYDVQQKEw9tdXNpY2FsLmx5IEluYy4xEDAOBgNVBAsTB2FuZHJvaWQxEzARBgNVBAMTCm11c2ljYWwubHkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCvEuNMCwMeQebJmsO2NtQlOqdYTrya5MWSRLApebgJaSefMubj3+AaDXy27UAA6JI92Q1xcaM3hk9qZMWQ2yBLqrl/AT/ox97+PKtMFrJMtpWaPP+5kFcjwKERbQAqnP1yHB56Fjg9R+J+1Dh/jcy6bkTVdB2ly3opX4wytSdQw+1aVvSU/z1mfKPVnJw1c79oVmdyebhNRdgMU7OpQZEau+mxXOpjar+bpj6ZssntevpI+i8JaB/dVZ9Hkuz1omBAAY76971BGtsNUuKlrYQkp3b1Q1g6fpJsawM3yqSu+iP5r+UsmTOh/G5zvJPbjU4qtFDC3pBkjdGc19CcVlK/AgMBAAGjITAfMB0GA1UdDgQWBBQi6xoMVEjV/7KBd/FF3/fs9sH+ITANBgkqhkiG9w0BAQsFAAOCAQEALzQnY5WC+mmWAssIZZuq9yLMEc4uKXhBMPa0alogY36uTxkQaiQzqthDPh8+JsKQSJVmb59PRhw283ApwlHHBg2cVUU605A3WjS7eFWQAjhZAEbXYY4CosyRvr+aH6250iC8ksGEcjw2a2y//pOLsK6AL5YPhwOdG3xhO6hCgoRbl/zsrXRo3s6j2Db3VFNpGT3wTXQGxqAtC/cN/zZHbT1LV4wikpa/ijVwSf5+V3mTcH0pQsSZiyM1wNqWiUr71jihfYY8l/f94qh4Rfh7dwIA3y6OtyCtP0+p/Oieop5s+i/Q08Tn6xDztKKaUNvqO9pv83jB0UvACa9m2woW0g==";
        sign = new Signature(Base64.decode(originalSignature, Base64.DEFAULT));
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("getPackageInfo") && args[0].toString().equals(packageName) && (((int)args[1] & 64) != 0)) {
            PackageInfo result = (PackageInfo) method.invoke(original, args);
            if (result != null) {
                result.signatures[0] = sign;
            }
        }
        return method.invoke(original, args);
    }
}
