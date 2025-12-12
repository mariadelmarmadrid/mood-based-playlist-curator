package org.wit.mood.views.moodlist

import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MoodListPresenter(
    private val view: MoodListView,
    private val app: MainApp
) {

    // State (same as your Activity)
    private var allMoods: List<MoodModel> = emptyList()
    private var queryText: String = ""
    private var dateFrom: LocalDate? = null
    private var dateTo: LocalDate? = null
    private var minDailyAvg: Double = -2.0

    private val dayFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var filterVisible = false

    fun onStart() {
        allMoods = app.moods.findAll()
        refresh()
    }

    fun onAddClicked() = view.navigateToAddMood()

    fun onMapClicked() = view.navigateToMap()

    fun onInsightsClicked() = view.navigateToInsights()

    fun onFilterClicked() {
        filterVisible = !filterVisible
        view.showFilterPanel(filterVisible)
    }

    fun onThemeClicked() {
        // keep theme toggle inside Activity (it needs Android APIs)
        // Presenter can just request it if you want:
        // view.toggleTheme()
    }

    fun onSearchChanged(text: String) {
        queryText = text
        refresh()
    }

    fun onAllDatesSelected() {
        dateFrom = null
        dateTo = null
        refresh()
    }

    fun onTodaySelected(today: LocalDate = LocalDate.now()) {
        dateFrom = today
        dateTo = today
        refresh()
    }

    fun onLast7Selected(today: LocalDate = LocalDate.now()) {
        dateFrom = today.minusDays(6)
        dateTo = today
        refresh()
    }

    fun onMinAvgChanged(pos: Int) {
        val threshold = thresholdFor(pos)
        val label = moodLabelFor(pos)

        minDailyAvg = threshold ?: -2.0

        view.updateMinAvgLabel(
            if (threshold == null) "Min daily average: All"
            else "Min daily average: $label"
        )
        refresh()
    }

    fun onResetFilters() {
        queryText = ""
        dateFrom = null
        dateTo = null
        minDailyAvg = -2.0
        refresh()
        filterVisible = false
        view.showFilterPanel(false)
        view.updateMinAvgLabel("Min daily average: All")
    }

    fun onApplyFilters() {
        refresh()
        filterVisible = false
        view.showFilterPanel(false)
    }

    fun onEditMoodRequested(mood: MoodModel) {
        view.navigateToEditMood(mood)
    }

    fun onDataChanged() {
        allMoods = app.moods.findAll()
        refresh()
    }

    private fun refresh() {
        val keywordFiltered = applyKeywordFilter(allMoods, queryText)
        val summaries = toDailySummaries(keywordFiltered)
        val finalDays = filterSummaries(summaries, dateFrom, dateTo, minDailyAvg)
        view.renderDays(finalDays)
    }

    private fun applyKeywordFilter(source: List<MoodModel>, query: String): List<MoodModel> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return source
        return source.filter { it.note.orEmpty().lowercase().contains(q) }
    }

    private fun toDailySummaries(list: List<MoodModel>): List<DailyMoodSummary> {
        val grouped = list.groupBy { it.timestamp.take(10) } // yyyy-MM-dd
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date }
    }

    private fun filterSummaries(
        days: List<DailyMoodSummary>,
        from: LocalDate?,
        to: LocalDate?,
        minAvg: Double
    ): List<DailyMoodSummary> {
        return days.filter { d ->
            val ld = runCatching { LocalDate.parse(d.date, dayFmt) }.getOrNull()
            val okDate = if (ld == null) true else {
                val gteFrom = from?.let { !ld.isBefore(it) } ?: true
                val lteTo = to?.let { !ld.isAfter(it) } ?: true
                gteFrom && lteTo
            }
            val okAvg = (minAvg <= -2.0) || (d.averageScore >= minAvg)
            okDate && okAvg
        }
    }

    private fun moodLabelFor(pos: Int): String = when (pos) {
        2 -> "Happy 😊"
        1 -> "Relaxed 🙂"
        0 -> "Neutral 😐"
        -1 -> "Sad 😢"
        -2 -> "All"
        else -> "All"
    }

    private fun thresholdFor(pos: Int): Double? = when (pos) {
        2 -> 1.5
        1 -> 0.5
        0 -> -0.5
        -1 -> -1.5
        -2 -> null
        else -> null
    }
}
