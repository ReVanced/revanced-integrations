package app.revanced.integrations.all.location.hide;

import android.location.Location;

public class HideMockLocationPatch {

    public static boolean isMock(Location location) {
        return false;
    }

    public static boolean isFromMockProvider(Location location) {
        return false;
    }
}
