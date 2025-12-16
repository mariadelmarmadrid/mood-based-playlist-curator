package org.wit.mood.views.moodlist

import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel

/**
 * MoodListContract
 *
 * Defines the MVP (Model-View-Presenter) contract for the Mood List feature.
 *
 * Separates responsibilities:
 *  - View: handles all UI rendering and navigation
 *  - Presenter: handles all business logic and user actions
 */
interface MoodListContract {

    /**
     * View interface for the Mood List screen.
     *
     * The Activity or Fragment implementing this interface
     * is responsible for rendering data and responding to UI events.
     */
    interface View {

        /**
         * Renders a list of daily mood summaries.
         *
         * @param days List of DailyMoodSummary objects (grouped by day)
         */
        fun renderList(days: List<DailyMoodSummary>)

        /**
         * Opens the Mood editor screen.
         *
         * @param mood The MoodModel to edit; null to create a new mood.
         */
        fun launchMoodEditor(mood: MoodModel? = null)

        /** Navigates to the Map view showing mood locations. */
        fun navigateToMap()

        /** Navigates to the Insights view showing statistics and trends. */
        fun navigateToInsights()

        /**
         * Applies night mode to the UI.
         *
         * @param enabled Whether night mode should be enabled
         */
        fun applyNightMode(enabled: Boolean)

        /** Closes the current view. */
        fun finishView()
    }

    /**
     * Presenter interface for the Mood List screen.
     *
     * Responsible for handling user actions and business logic,
     * providing data to the View, and managing navigation.
     */
    interface Presenter {

        /** Loads all moods from the data store and groups them by day. */
        fun loadMoods()

        /** Opens the screen to add a new mood. */
        fun openAddMood()

        /**
         * Opens the editor for an existing mood.
         *
         * @param mood MoodModel to edit
         */
        fun openEditMood(mood: MoodModel)

        /** Opens the Map view showing moods with locations. */
        fun openMap()

        /** Opens the Insights view showing statistics and trends. */
        fun openInsights()

        /** Loads the current night mode setting. */
        fun loadNightMode()

        /** Toggles night mode on or off. */
        fun doToggleNightMode()

        /**
         * Launches the Mood editor via an Android Intent.
         *
         * @param intent Intent to start the editor
         */
        fun launchEditor(intent: android.content.Intent)
    }
}
