package app.revanced.integrations.syncforreddit.internal

import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object RedditVideoPlaylistParser {
    class MpdEntry(val bandwidth: Int, val baseUrl: String)

    private fun getBestMpEntry(element: Element): MpdEntry? {
        val representations = element.getElementsByTagName("Representation")
        val entries = mutableListOf<MpdEntry>()
        for (i in 0 until representations.length) {
            val representation = representations.item(i) as Element
            val bandwidth = representation.getAttribute("bandwidth")?.toIntOrNull()
            val baseUrl = representation.getElementsByTagName("BaseURL").item(0)

            if (bandwidth != null && baseUrl != null) {
                entries.add(MpdEntry(bandwidth, baseUrl.textContent))
            }
        }

        return entries.maxByOrNull { it.bandwidth }
    }

    fun parse(data: ByteArray): Array<String?> {
        val adaptionSets = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(ByteArrayInputStream(data))
            .getElementsByTagName("AdaptationSet")

        var videoUrl: String? = null
        var audioUrl: String? = null

        for (i in 0 until adaptionSets.length) {
            val element = adaptionSets.item(i) as Element
            val contentType = element.getAttribute("contentType")
            val bestEntry = getBestMpEntry(element) ?: continue

            when (contentType) {
                "video" -> videoUrl = bestEntry.baseUrl
                "audio" -> audioUrl = bestEntry.baseUrl
            }
        }

        return arrayOf(videoUrl, audioUrl)
    }
}


