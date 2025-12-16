package org.wit.mood.views.insights

import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodType

/**
 * InsightsPresenter
 *
 * Presenter for the Insights screen (MVP pattern).
 *
 * Responsibilities:
 *  - Prepare daily mood summaries from the app’s stored moods
 *  - Determine average mood per day and map to emoji labels
 *  - Provide playlist URLs based on average mood
 *  - Handle day navigation (previous/next)
 *  - Update the View with ring chart, legend, playlist button, and day info
 *
 * @param view The Insights View interface
 * @param app Reference to MainApp for data access
 */
class InsightsPresenter(private val view: InsightsContract.View, private val app: MainApp) {

    // List of daily summaries to display
    private var days: List<DailyMoodSummary> = emptyList()

    // Index of the currently displayed day
    private var index: Int = 0

    // URL for the playlist corresponding to the current day's average mood
    private var currentPlaylistUrl: String? = null

    /**
     * Load daily summaries from the stored moods and render the first day.
     */
    fun load() {
        days = getDailySummaries()
        index = 0
        render()
    }

    /** Navigate to the previous day (if available) and re-render. */
    fun onPrevDay() {
        if (index < days.lastIndex) {
            index++
            render()
        }
    }

    /** Navigate to the next day (if available) and re-render. */
    fun onNextDay() {
        if (index > 0) {
            index--
            render()
        }
    }

    /** Open the playlist for the current day in Spotify or browser. */
    fun onOpenPlaylist() {
        val url = currentPlaylistUrl ?: return
        view.openUrlInSpotifyOrBrowser(url)
    }

    // ----- Core rendering logic -----

    /**
     * Render the current day summary in the view.
     *
     * Handles:
     *  - Empty state if no moods exist
     *  - Average mood label
     *  - Playlist button visibility
     *  - Mood ring chart
     *  - Mood legend
     *  - Previous/next day navigation buttons
     */
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

        // Update the View with day info and average label
        view.showDay(day.date, avgLabel)

        // Set playlist URL and button visibility
        currentPlaylistUrl = playlistUrlFor(avgScore)
        view.showPlaylistButton(currentPlaylistUrl != null)

        // Count each MoodType for the ring chart and legend
        val counts = MoodType.values().associateWith { mood ->
            day.moods.count { it.type == mood }
        }
        view.updateRing(counts, avgLabel)
        view.renderLegend(counts)

        // Enable/disable navigation buttons
        view.enableDayNav(
            prevEnabled = index < days.lastIndex,
            nextEnabled = index > 0
        )
    }

    /**
     * Maps an average mood score to a label with emoji.
     *
     * @param avgScore Average mood score for the day
     * @return Label representing mood (e.g., "Relaxed 😌")
     */
    private fun avgLabelFor(avgScore: Double): String = when {
        avgScore >= 1.5  -> "Happy 😊"
        avgScore >= 0.5  -> "Relaxed 😌"
        avgScore >= -0.5 -> "Neutral 😐"
        avgScore >= -1.5 -> "Sad 😢"
        else             -> "Angry 😠"
    }

    /**
     * Collects daily summaries from all stored moods.
     *
     * Groups moods by date (yyyy-MM-dd), sorts moods in each day descending by timestamp,
     * and calculates the average score for each day.
     *
     * @return List of DailyMoodSummary sorted by date descending
     */
    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // yyyy-MM-dd
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date }
    }

    /**
     * Maps an average mood score to a Spotify playlist URL.
     *
     * @param avgScore Average mood score for the day
     * @return Spotify playlist URL or null if unavailable
     */
    private fun playlistUrlFor(avgScore: Double): String? = when {
        avgScore >= 1.5  -> "https://open.spotify.com/playlist/0jrlHA5UmxRxJjoykf7qRY?si=nIQRZZctSweuaJ-hFQ0ezA&pi=wgl1rOciSwSfm"
        avgScore >= 0.5  -> "https://open.spotify.com/playlist/7mBi5NbnmRIw60o8GCWHDg?si=6aFrE9oBQ3KYxkXErRcSFg&pi=EHZ5weKLSWyBn"
        avgScore >= -0.5 -> "https://open.spotify.com/playlist/37i9dQZF1EIcJuX6lvhrpW?si=UQi7HmGwQHmSKT-8ZJhBMQ&pi=w8QhyZiIRAmC8"
        avgScore >= -1.5 -> "https://open.spotify.com/playlist/4bRQf8bwAIVgCb6Lcoursx?si=VHWLE9zPTSGNxfVk1OZ2JQ&pi=PoH0HGsGQVSKH"
        else             -> "https://open.spotify.com/playlist/67STztGl7srSMNn6hVYPFR?si=YzgRdjzXTK-_pxca1ijBtA&pi=PSAWWS2PT_O8A"
    }
}
