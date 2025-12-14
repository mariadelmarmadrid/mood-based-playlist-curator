package org.wit.mood.views.insights

import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodType

class InsightsPresenter(private val view: InsightsContract.View, private val app: MainApp) {

    private var days: List<DailyMoodSummary> = emptyList()
    private var index: Int = 0
    private var currentPlaylistUrl: String? = null

    fun load() {
        days = getDailySummaries()
        index = 0
        render()
    }

    fun onPrevDay() {
        if (index < days.lastIndex) {
            index++
            render()
        }
    }

    fun onNextDay() {
        if (index > 0) {
            index--
            render()
        }
    }

    fun onOpenPlaylist() {
        val url = currentPlaylistUrl ?: return
        view.openUrlInSpotifyOrBrowser(url)
    }

    // ----- Core render -----

    private fun render() {
        if (days.isEmpty()) {
            currentPlaylistUrl = null
            view.showEmptyState()
            view.showPlaylistButton(false)
            view.enableDayNav(prevEnabled = false, nextEnabled = false)
            return
        }

        val day = days[index]
        val avgScore = day.averageScore
        val avgLabel = avgLabelFor(avgScore)

        view.showDay(day.date, avgLabel)

        // playlist
        currentPlaylistUrl = playlistUrlFor(avgScore)
        view.showPlaylistButton(currentPlaylistUrl != null)

        // counts + ring + legend
        val counts = MoodType.values().associateWith { mood ->
            day.moods.count { it.type == mood }
        }
        view.updateRing(counts, avgLabel)
        view.renderLegend(counts)

        view.enableDayNav(
            prevEnabled = index < days.lastIndex,
            nextEnabled = index > 0
        )
    }

    private fun avgLabelFor(avgScore: Double): String = when {
        avgScore >= 1.5  -> "Happy 😊"
        avgScore >= 0.5  -> "Relaxed 😌"
        avgScore >= -0.5 -> "Neutral 😐"
        avgScore >= -1.5 -> "Sad 😢"
        else             -> "Angry 😠"
    }

    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // yyyy-MM-dd
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date }
    }

    private fun playlistUrlFor(avgScore: Double): String? = when {
        avgScore >= 1.5  -> "https://open.spotify.com/playlist/0jrlHA5UmxRxJjoykf7qRY?si=nIQRZZctSweuaJ-hFQ0ezA&pi=wgl1rOciSwSfm"
        avgScore >= 0.5  -> "https://open.spotify.com/playlist/7mBi5NbnmRIw60o8GCWHDg?si=6aFrE9oBQ3KYxkXErRcSFg&pi=EHZ5weKLSWyBn"
        avgScore >= -0.5 -> "https://open.spotify.com/playlist/37i9dQZF1EIcJuX6lvhrpW?si=UQi7HmGwQHmSKT-8ZJhBMQ&pi=w8QhyZiIRAmC8"
        avgScore >= -1.5 -> "https://open.spotify.com/playlist/4bRQf8bwAIVgCb6Lcoursx?si=VHWLE9zPTSGNxfVk1OZ2JQ&pi=PoH0HGsGQVSKH"
        else             -> "https://open.spotify.com/playlist/67STztGl7srSMNn6hVYPFR?si=YzgRdjzXTK-_pxca1ijBtA&pi=PSAWWS2PT_O8A"
    }
}
