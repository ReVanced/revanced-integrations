package app.revanced.twitter.utils.json

import app.revanced.twitter.utils.stream.StreamUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object JsonUtils {
    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun parseJson(jsonInputStream: InputStream) = JSONObject(StreamUtils.toString(jsonInputStream))
}