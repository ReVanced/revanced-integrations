package app.revanced.integrations.tiktok;

import app.revanced.integrations.shared.settings.Setting;

public class Utils {
    public static long[] parseMinMax(Setting setting) {
        if (setting.returnType == Setting.ReturnType.STRING) {
            final String[] minMax = setting.getString().split("-");

            if (minMax.length == 2)
                try {
                    final long min = Long.parseLong(minMax[0]);
                    final long max = Long.parseLong(minMax[1]);

                    if (min <= max && min >= 0) return new long[]{min, max};

                } catch (NumberFormatException ignored) {
                }
        }

        setting.saveValue("0-" + Long.MAX_VALUE);
        return new long[]{0L, Long.MAX_VALUE};
    }
}