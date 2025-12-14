package org.wit.mood.views.insights

import org.wit.mood.models.MoodType

interface InsightsContract {

    interface View {
        fun showEmptyState()
        fun showDay(date: String, averageLabel: String)
        fun showPlaylistButton(show: Boolean)
        fun enableDayNav(prevEnabled: Boolean, nextEnabled: Boolean)
        fun updateRing(counts: Map<MoodType, Int>, averageLabel: String)
        fun renderLegend(counts: Map<MoodType, Int>)
        fun openUrlInSpotifyOrBrowser(url: String)
        fun openHome()
        fun openAddMood()
    }
}
