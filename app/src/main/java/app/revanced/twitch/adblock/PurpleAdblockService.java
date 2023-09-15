package app.revanced.twitch.adblock;

import app.revanced.twitch.api.RetrofitClient;
import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.TwitchUtils;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.util.HashMap;
import java.util.Map;

public class PurpleAdblockService implements IAdblockService {
    private final Map<String, Boolean> tunnels = new HashMap<>() {{
        put("https://eu1.jupter.ga", false);
        put("https://eu2.jupter.ga", false);
    }};

    @Override
    public String friendlyName() {
        return TwitchUtils.getString("revanced_proxy_purpleadblock");
    }

    @Override
    public Integer maxAttempts() {
        return 3;
    }

    @Override
    public Boolean isAvailable() {
        for (String tunnel : tunnels.keySet()) {
            var success = true;

            try {
                var response = RetrofitClient.getInstance().getPurpleAdblockApi().ping(tunnel).execute();
                if (!response.isSuccessful()) {
                    LogHelper.error("PurpleAdBlock tunnel $tunnel returned an error: HTTP code %d", response.code());
                    LogHelper.debug(response.message());

                    try (var errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            LogHelper.debug(errorBody.string());
                        }
                    }

                    success = false;
                }
            } catch (Exception ex) {
                LogHelper.printException("PurpleAdBlock tunnel $tunnel is unavailable", ex);
                success = false;
            }

            // Cache availability data
            tunnels.put(tunnel, success);

            if (success)
                return true;
        }

        return false;
    }

    @Override
    public Request rewriteHlsRequest(Request originalRequest) {
        for (Map.Entry<String, Boolean> entry : tunnels.entrySet()) {
            if (!entry.getValue()) continue;

            var server = entry.getKey();

            // Compose new URL
            var url = HttpUrl.parse(server + "/channel/" + IAdblockService.channelName(originalRequest));
            if (url == null) {
                LogHelper.error("Failed to parse rewritten URL");
                return null;
            }

            // Overwrite old request
            return new Request.Builder()
                    .get()
                    .url(url)
                    .build();
        }

        LogHelper.error("No tunnels are available");
        return null;
    }
}
