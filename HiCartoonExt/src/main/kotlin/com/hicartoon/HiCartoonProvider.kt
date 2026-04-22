package com.hicartoon

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.Jsoup

class HiCartoonProvider : MainAPI() {
    override var mainUrl = "https://hicartoon.to"
    override var name = "HiCartoon"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    
    override val supportedTypes = setOf(
        TvType.TvSeries,
        TvType.Movie,
        TvType.NSFW
    )

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val link = "$mainUrl/search?keyword=$query"
            val html = app.get(link).text
            val document = Jsoup.parse(html)

            document.select(".film-list .item, .flw-item").mapNotNull { element ->
                val title = element.selectFirst(".film-name a, .title")?.text() ?: return@mapNotNull null
                val href = fixUrlNull(element.selectFirst("a")?.attr("href")) ?: return@mapNotNull null
                val posterUrl = fixUrlNull(element.selectFirst("img")?.attr("src") ?: element.selectFirst("img")?.attr("data-src"))
                val typeText = element.selectFirst(".fdi-type, .type")?.text() ?: ""
                
                if (typeText.contains("Movie", ignoreCase = true)) {
                    newMovieSearchResponse(title, href, TvType.Movie) {
                        this.posterUrl = posterUrl
                    }
                } else {
                    newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                        this.posterUrl = posterUrl
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        return try {
            val html = app.get(url).text
            val document = Jsoup.parse(html)

            val title = document.selectFirst(".detail-title, .heading-name")?.text() ?: return null
            val posterUrl = fixUrlNull(document.selectFirst(".film-poster img")?.attr("src"))
            val plot = document.selectFirst(".description, .film-description")?.text()
            val yearText = document.selectFirst(".year, .release-year")?.text()
            val year = yearText?.filter { it.isDigit() }?.toIntOrNull()

            val episodesList = document.select(".episodes-list li a, .ep-item")
            val isMovie = episodesList.isEmpty()

            if (isMovie) {
                val watchUrl = document.selectFirst(".play-btn, #watch-iframe")?.attr("href") ?: url
                newMovieLoadResponse(title, url, TvType.Movie, watchUrl) {
                    this.posterUrl = posterUrl
                    this.plot = plot
                    this.year = year
                }
            } else {
                val episodes = episodesList.mapNotNull { ep ->
                    val epHref = fixUrlNull(ep.attr("href")) ?: return@mapNotNull null
                    val epTitle = ep.attr("title").ifEmpty { ep.text() }
                    val epNum = ep.attr("data-number").toIntOrNull() ?: ep.text().filter { it.isDigit() }.toIntOrNull()

                    Episode(
                        data = epHref,
                        name = epTitle,
                        episode = epNum
                    )
                }

                newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                    this.posterUrl = posterUrl
                    this.plot = plot
                    this.year = year
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val html = app.get(data).text
            val document = Jsoup.parse(html)

            val iframeSrc = document.selectFirst("iframe")?.attr("src")

            if (iframeSrc != null && iframeSrc.startsWith("http")) {
                val iframeHtml = app.get(iframeSrc).text
                extractFromHtml(iframeHtml, iframeSrc, callback)
            } else {
                val scriptTagsHtml = document.select("script").joinToString("") { it.html() }
                extractFromHtml(scriptTagsHtml, data, callback)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun extractFromHtml(html: String, referer: String, callback: (ExtractorLink) -> Unit) {
        val m3u8Regex = Regex("[\"'](https?://[^\"]+\\.m3u8[^\"]*)[\"']")
        val mp4Regex = Regex("[\"'](https?://[^\"]+\\.mp4[^\"]*)[\"']")

        val m3u8Match = m3u8Regex.find(html)
        val mp4Match = mp4Regex.find(html)

        if (m3u8Match != null) {
            val m3u8Url = m3u8Match.groupValues[1]
            M3u8Helper.generateM3u8(
                name = this.name,
                m3u8Url = m3u8Url,
                referer = referer
            ).forEach(callback)
        } else if (mp4Match != null) {
            val mp4Url = mp4Match.groupValues[1]
            callback.invoke(
                ExtractorLink(
                    this.name,
                    this.name,
                    mp4Url,
                    referer,
                    Qualities.Unknown.value,
                    isM3u8 = false
                )
            )
        }
    }
}
