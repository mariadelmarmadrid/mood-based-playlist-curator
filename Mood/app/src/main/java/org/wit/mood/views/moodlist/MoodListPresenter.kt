package org.wit.mood.views.moodlist

import android.content.Context
import android.content.SharedPreferences
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Date filter options for mood list filtering.
 */
enum class DateFilter {
    ALL, TODAY, LAST_7_DAYS
}

/**
 * MoodListPresenter
 *
 * Presenter class for the Mood List feature (MVP pattern).
 */
class MoodListPresenter(
    private val view: MoodListContract.View,
    context: Context
) : MoodListContract.Presenter {

    // Application reference
    private val app: MainApp = context.applicationContext as MainApp

    // SharedPreferences for night mode
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // ---------------------------------------------------------------------
    // Filter state
    // ---------------------------------------------------------------------

    private var searchQuery = ""
    private var dateFilter = DateFilter.ALL
    private var minDailyAverage = -2

    // ---------------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------------

    init {
        loadMoods()
    }

    // ---------------------------------------------------------------------
    // Data loading & filtering
    // ---------------------------------------------------------------------

    override fun loadMoods() {
        val moods = app.moods.findAll()
        val summaries = DailyMoodSummary.fromMoods(moods)
        view.renderList(summaries)
    }

    override fun applyFilters() {
        val allMoods = app.moods.findAll()

        val filtered = allMoods.filter { mood ->
            matchesSearch(mood) && matchesDate(mood)
        }

        val summaries = DailyMoodSummary
            .fromMoods(filtered)
            .filter { it.averageScore >= minDailyAverage }

        view.renderList(summaries)
    }

    override fun resetFilters() {
        searchQuery = ""
        dateFilter = DateFilter.ALL
        minDailyAverage = -2
        loadMoods()
    }

    // ---------------------------------------------------------------------
    // Filter setters
    // ---------------------------------------------------------------------

    override fun onSearchQueryChanged(query: String) {
        searchQuery = query.lowercase()
    }

    override fun onDateFilterChanged(filter: DateFilter) {
        dateFilter = filter
    }

    override fun onMinAverageChanged(value: Int) {
        minDailyAverage = value
    }

    // ---------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------

    override fun openAddMood() {
        view.launchMoodEditor()
    }

    override fun openEditMood(mood: MoodModel) {
        view.launchMoodEditor(mood)
    }

    override fun openMap() {
        view.navigateToMap()
    }

    override fun openInsights() {
        view.navigateToInsights()
        view.finishView()
    }

    // ---------------------------------------------------------------------
    // Night mode
    // ---------------------------------------------------------------------

    override fun loadNightMode() {
        view.applyNightMode(prefs.getBoolean("night_mode", false))
    }

    override fun doToggleNightMode() {
        val next = !prefs.getBoolean("night_mode", false)
        prefs.edit().putBoolean("night_mode", next).apply()
        view.applyNightMode(next)
    }

    // ---------------------------------------------------------------------
    // Helper filter logic
    // ---------------------------------------------------------------------

    private fun matchesSearch(mood: MoodModel): Boolean {
        return searchQuery.isBlank() ||
                mood.note.lowercase().contains(searchQuery)
    }

    private fun matchesDate(mood: MoodModel): Boolean {
        val time = parseTimestamp(mood.timestamp)

        return when (dateFilter) {
            DateFilter.ALL -> true
            DateFilter.TODAY -> isToday(time)
            DateFilter.LAST_7_DAYS -> isWithinLast7Days(time)
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinLast7Days(timestamp: Long): Boolean {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        return timestamp >= sevenDaysAgo
    }

    private fun parseTimestamp(timestamp: String): Long {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        return formatter.parse(timestamp)?.time ?: 0L
    }
}
