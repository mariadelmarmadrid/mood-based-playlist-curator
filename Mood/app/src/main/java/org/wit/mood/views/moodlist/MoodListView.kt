package org.wit.mood.views.moodlist

import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel

interface MoodListView {
    fun renderDays(days: List<DailyMoodSummary>)
    fun navigateToAddMood()
    fun navigateToEditMood(mood: MoodModel)
    fun navigateToMap()
    fun navigateToInsights()
    fun showFilterPanel(show: Boolean)
    fun updateMinAvgLabel(text: String)
}
