package org.wit.mood.views.moodlist

import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel

/**
 * MoodListContract
 *
 * Defines the MVP (Model–View–Presenter) contract for the Mood List feature.
 *
 * This contract clearly separates responsibilities:
 *  - View: responsible for UI rendering, user interaction, and navigation
 *  - Presenter: responsible for business logic, filtering, and state management
 *
 * Using a contract improves testability, readability, and maintainability.
 */
interface MoodListContract {

    /**
     * View interface for the Mood List screen.
     *
     * Implemented by [MoodListView].
     * The View contains only Android/UI-related logic and delegates
     * all business decisions to the Presenter.
     */
    interface View {

        /**
         * Renders a list of daily mood summaries on screen.
         *
         * Each item represents one calendar day and contains:
         *  - All moods recorded that day
         *  - The calculated daily average mood score
         *
         * @param days List of grouped DailyMoodSummary objects
         */
        fun renderList(days: List<DailyMoodSummary>)

        /**
         * Opens the Mood editor screen.
         *
         * If a MoodModel is provided, the editor opens in edit mode.
         * If null, a new mood entry is created.
         *
         * @param mood Optional MoodModel to edit
         */
        fun launchMoodEditor(mood: MoodModel? = null)

        /**
         * Navigates to the Map screen, displaying moods
         * that have an associated location.
         */
        fun navigateToMap()

        /**
         * Navigates to the Insights screen, which displays
         * visual summaries, charts, and playlist recommendations.
         */
        fun navigateToInsights()

        /**
         * Applies the selected night mode setting to the UI.
         *
         * This method updates the app theme but does not
         * persist the setting (handled by the Presenter).
         *
         * @param enabled True to enable dark mode, false otherwise
         */
        fun applyNightMode(enabled: Boolean)

        /**
         * Closes the current screen.
         *
         * Typically used when navigating away from the Mood List
         * to another primary destination.
         */
        fun finishView()
    }

    /**
     * Presenter interface for the Mood List screen.
     *
     * Implemented by [MoodListPresenter].
     * The Presenter contains all business logic and application state,
     * and never directly accesses Android UI components.
     */
    interface Presenter {

        /**
         * Loads all mood entries from the data store,
         * groups them by date, and sends them to the View.
         */
        fun loadMoods()

        /**
         * Requests navigation to the Add Mood screen.
         */
        fun openAddMood()

        /**
         * Requests navigation to the Edit Mood screen
         * for the selected mood entry.
         *
         * @param mood MoodModel to edit
         */
        fun openEditMood(mood: MoodModel)

        /**
         * Requests navigation to the Map screen.
         */
        fun openMap()

        /**
         * Requests navigation to the Insights screen.
         */
        fun openInsights()

        /**
         * Loads the current night mode preference
         * and applies it through the View.
         */
        fun loadNightMode()

        /**
         * Toggles night mode on or off and persists
         * the new preference.
         */
        fun doToggleNightMode()

        /**
         * Updates the search query used for filtering moods
         * based on the user's note text input.
         *
         * @param query Text entered by the user
         */
        fun onSearchQueryChanged(query: String)

        /**
         * Updates the active date filter
         * (All, Today, or Last 7 Days).
         *
         * @param filter Selected DateFilter value
         */
        fun onDateFilterChanged(filter: DateFilter)

        /**
         * Updates the minimum daily average mood score
         * used for filtering the results.
         *
         * @param value Minimum allowed daily average
         */
        fun onMinAverageChanged(value: Int)

        /**
         * Applies all currently active filters
         * (search text, date range, and minimum average)
         * and updates the View with the results.
         */
        fun applyFilters()

        /**
         * Resets all filters to their default values
         * and reloads the full mood list.
         */
        fun resetFilters()
    }
}
