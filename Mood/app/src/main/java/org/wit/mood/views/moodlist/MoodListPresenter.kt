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
 * DateFilter
 *
 * Represents the available date-based filtering options
 * for the Mood List screen.
 */
enum class DateFilter {
    ALL,        // Show all recorded moods
    TODAY,      // Show moods recorded today
    LAST_7_DAYS // Show moods recorded in the last 7 days
}

/**
 * MoodListPresenter
 *
 * Presenter for the Mood List feature, implementing the Presenter
 * role in the MVP (Model–View–Presenter) architecture.
 *
 * Responsibilities:
 *  - Load mood data from persistent storage
 *  - Apply search, date, and average-based filters
 *  - Group moods by calendar day and calculate daily averages
 *  - Handle navigation requests from the View
 *  - Manage night mode preference logic
 *
 * The Presenter contains no Android UI code and communicates
 * exclusively with the View through the MoodListContract.
 */
class MoodListPresenter(
    private val view: MoodListContract.View,
    context: Context
) : MoodListContract.Presenter {

    /**
     * Reference to the Application instance.
     *
     * Used to access the mood data store while avoiding
     * direct dependency on an Activity.
     */
    private val app: MainApp = context.applicationContext as MainApp

    /**
     * SharedPreferences used to persist the night mode setting.
     */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // ---------------------------------------------------------------------
    // Filter state
    // ---------------------------------------------------------------------

    /** Current text query used to filter mood notes. */
    private var searchQuery = ""

    /** Current date filter selection. */
    private var dateFilter = DateFilter.ALL

    /** Minimum daily average mood score required to display a day. */
    private var minDailyAverage = -2

    // ---------------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------------

    /**
     * Loads the initial mood list when the presenter is created.
     *
     * This ensures the Mood List screen is populated immediately.
     */
    init {
        loadMoods()
    }

    // ---------------------------------------------------------------------
    // Data loading & filtering
    // ---------------------------------------------------------------------

    /**
     * Loads all moods from the data store, groups them by day,
     * and sends the results to the View for rendering.
     */
    override fun loadMoods() {
        val moods = app.moods.findAll()
        val summaries = buildDailySummaries(moods)
        view.renderList(summaries)
    }

    /**
     * Applies all active filters (search text, date range,
     * and minimum daily average) and updates the View.
     *
     * Filtering behaviour matches Assignment 1, providing
     * instant feedback to the user.
     */
    override fun applyFilters() {
        val allMoods = app.moods.findAll()

        val filtered = allMoods.filter { mood ->
            matchesSearch(mood) && matchesDate(mood)
        }

        val summaries = buildDailySummaries(filtered)
            .filter { it.averageScore >= minDailyAverage }

        view.renderList(summaries)
    }

    /**
     * Resets all filter values to their defaults and reloads
     * the full, unfiltered mood list.
     */
    override fun resetFilters() {
        searchQuery = ""
        dateFilter = DateFilter.ALL
        minDailyAverage = -2
        loadMoods()
    }

    // ---------------------------------------------------------------------
    // Filter setters (called from the View)
    // ---------------------------------------------------------------------

    /**
     * Updates the current search query used to filter mood notes.
     *
     * @param query Text entered by the user
     */
    override fun onSearchQueryChanged(query: String) {
        searchQuery = query.lowercase()
    }

    /**
     * Updates the currently selected date filter.
     *
     * @param filter Selected DateFilter value
     */
    override fun onDateFilterChanged(filter: DateFilter) {
        dateFilter = filter
    }

    /**
     * Updates the minimum daily average score threshold.
     *
     * @param value Minimum average mood score
     */
    override fun onMinAverageChanged(value: Int) {
        minDailyAverage = value
    }

    // ---------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------

    /**
     * Requests navigation to the Add Mood screen.
     */
    override fun openAddMood() {
        view.launchMoodEditor()
    }

    /**
     * Requests navigation to the Edit Mood screen
     * for the selected mood entry.
     *
     * @param mood MoodModel to edit
     */
    override fun openEditMood(mood: MoodModel) {
        view.launchMoodEditor(mood)
    }

    /**
     * Requests navigation to the Map screen.
     */
    override fun openMap() {
        view.navigateToMap()
    }

    /**
     * Requests navigation to the Insights screen and
     * closes the current Mood List view.
     */
    override fun openInsights() {
        view.navigateToInsights()
        view.finishView()
    }

    // ---------------------------------------------------------------------
    // Night mode
    // ---------------------------------------------------------------------

    /**
     * Loads the persisted night mode setting and applies it
     * through the View.
     */
    override fun loadNightMode() {
        view.applyNightMode(prefs.getBoolean("night_mode", false))
    }

    /**
     * Toggles the night mode setting, persists the new value,
     * and updates the UI.
     */
    override fun doToggleNightMode() {
        val next = !prefs.getBoolean("night_mode", false)
        prefs.edit().putBoolean("night_mode", next).apply()
        view.applyNightMode(next)
    }

    // ---------------------------------------------------------------------
    // Helper filter logic
    // ---------------------------------------------------------------------

    /**
     * Checks whether a mood matches the current text search query.
     */
    private fun matchesSearch(mood: MoodModel): Boolean {
        return searchQuery.isBlank() ||
                mood.note.lowercase().contains(searchQuery)
    }

    /**
     * Checks whether a mood matches the selected date filter.
     */
    private fun matchesDate(mood: MoodModel): Boolean {
        val time = parseTimestamp(mood.timestamp)

        return when (dateFilter) {
            DateFilter.ALL -> true
            DateFilter.TODAY -> isToday(time)
            DateFilter.LAST_7_DAYS -> isWithinLast7Days(time)
        }
    }

    /**
     * Determines whether a timestamp represents today's date.
     */
    private fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Determines whether a timestamp falls within the last 7 days.
     */
    private fun isWithinLast7Days(timestamp: Long): Boolean {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        return timestamp >= sevenDaysAgo
    }

    /**
     * Converts a timestamp string into milliseconds.
     *
     * The timestamp format matches the one used in MoodModel:
     * "yyyy-MM-dd HH:mm:ss".
     */
    private fun parseTimestamp(timestamp: String): Long {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        return formatter.parse(timestamp)?.time ?: 0L
    }

    /**
     * Groups a list of moods by calendar day and calculates
     * the average mood score for each day.
     *
     * This logic is kept inside the Presenter because it is
     * specific to the Mood List screen.
     */
    private fun buildDailySummaries(
        moods: List<MoodModel>
    ): List<DailyMoodSummary> {

        return moods
            .groupBy { it.timestamp.substring(0, 10) }
            .map { (date, moodsInDay) ->

                val sortedMoods =
                    moodsInDay.sortedByDescending { it.timestamp }

                val average =
                    if (sortedMoods.isNotEmpty())
                        sortedMoods.map { it.type.score }.average()
                    else
                        0.0

                DailyMoodSummary(
                    date = date,
                    moods = sortedMoods,
                    averageScore = average
                )
            }
            .sortedByDescending { it.date }
    }
}
