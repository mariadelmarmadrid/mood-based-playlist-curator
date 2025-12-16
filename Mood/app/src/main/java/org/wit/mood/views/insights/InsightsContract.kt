package org.wit.mood.views.insights

import org.wit.mood.models.MoodType

/**
 * InsightsContract
 *
 * Defines the contract (View interface) for the Insights feature in MVP pattern.
 *
 * Responsibilities of the View:
 *  - Display mood statistics and trends
 *  - Show or hide UI elements depending on available data
 *  - Handle navigation and external actions (e.g., Spotify link)
 */
interface InsightsContract {

    /**
     * View interface for the Insights screen.
     *
     * Activities or Fragments implementing this interface
     * will be responsible for rendering charts, legends, and navigation buttons.
     */
    interface View {

        /** Displays an empty state if there are no moods to show. */
        fun showEmptyState()

        /**
         * Shows information for a specific day.
         *
         * @param date The date string (e.g., "2025-12-16")
         * @param averageLabel The average mood label for that day (e.g., "Relaxed 😌")
         */
        fun showDay(date: String, averageLabel: String)

        /**
         * Shows or hides a playlist button for the current day.
         *
         * @param show Whether the playlist button should be visible
         */
        fun showPlaylistButton(show: Boolean)

        /**
         * Enables or disables day navigation buttons (previous/next).
         *
         * @param prevEnabled Enable the "previous day" button
         * @param nextEnabled Enable the "next day" button
         */
        fun enableDayNav(prevEnabled: Boolean, nextEnabled: Boolean)

        /**
         * Updates a mood ring chart showing counts for each MoodType.
         *
         * @param counts Map of MoodType to its count for the day
         * @param averageLabel Average mood label (e.g., "Neutral 😐")
         */
        fun updateRing(counts: Map<MoodType, Int>, averageLabel: String)

        /**
         * Renders a legend showing counts of each mood type.
         *
         * @param counts Map of MoodType to its count
         */
        fun renderLegend(counts: Map<MoodType, Int>)

        /**
         * Opens a URL in Spotify app or browser.
         *
         * @param url URL to open
         */
        fun openUrlInSpotifyOrBrowser(url: String)

        /** Navigates back to the Home screen. */
        fun openHome()

        /** Opens the Add Mood screen. */
        fun openAddMood()
    }
}
