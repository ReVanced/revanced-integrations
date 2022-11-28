package app.revanced.integrations.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requester {
    private Requester() {
    }

    public static HttpURLConnection getConnectionFromRoute(String apiUrl, Route route, String... params) throws IOException {
        String url = apiUrl + route.compile(params).getCompiledRoute();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(route.getMethod().name());
        // TODO: change the user agent string
        connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");

        return connection;
    }

    /**
     * Parses the connection stream, and then and disconnects the {@link HttpURLConnection}
     */
    // maybe rename this to parseJsonAndDisconnect
    public static String parseJson(HttpURLConnection connection) throws IOException {
        String result = parseJson(connection.getInputStream(), false);
        connection.disconnect();
        return result;
    }

    /**
     * Parse, and then close the {@link InputStream}
     */
    public static String parseJson(InputStream inputStream, boolean isError) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
            if (isError)
                jsonBuilder.append("\n");
        }
        inputStream.close();
        return jsonBuilder.toString();
    }

    /**
     * Parse the underlying {@link InputStream}, but do NOT disconnect the {@link HttpURLConnection}
     */
    public static String parseErrorJson(HttpURLConnection connection) throws IOException {
        return parseJson(connection.getErrorStream(), true);
    }

    /**
     * Parse a {@link JSONObject} and disconnects the {@link HttpURLConnection}
     */
    // maybe rename this to getJSONObjectAndDisconnect
    public static JSONObject getJSONObject(HttpURLConnection connection) throws JSONException, IOException {
        return new JSONObject(parseJsonAndDisconnect(connection));
    }

    /**
     * Parse a {@link JSONArray} and then disconnect the {@link HttpURLConnection}
     */
    // maybe rename this to getJSONArrayAndDisconnect
    public static JSONArray getJSONArray(HttpURLConnection connection) throws JSONException, IOException  {
        return new JSONArray(parseJsonAndDisconnect(connection));
    }

    private static String parseJsonAndDisconnect(HttpURLConnection connection) throws IOException {
        String json = parseJson(connection);
        connection.disconnect();
        return json;
    }
}