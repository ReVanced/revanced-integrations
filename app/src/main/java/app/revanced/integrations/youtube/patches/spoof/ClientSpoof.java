package app.revanced.integrations.youtube.patches.spoof;

import app.revanced.integrations.shared.Logger;
import com.google.protos.youtube.api.innertube.InnertubeContext$ClientInfo;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ClientSpoof {
    public static void spoofIos(InnertubeContext$ClientInfo info) {
        log();

        Logger.printInfo(() -> "Spoofed to IOS");
        info.r = 5;
    }


    public static synchronized void log() {
        var s = new StringWriter();
        var w = new PrintWriter(s);
        new Throwable().printStackTrace(w);
        Logger.printInfo(s::toString);
    }
}
