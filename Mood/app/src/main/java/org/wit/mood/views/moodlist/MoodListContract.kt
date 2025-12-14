package org.wit.mood.views.moodlist

import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel

interface MoodListContract {

    interface View {
        fun renderList(days: List<DailyMoodSummary>)
        fun launchMoodEditor(mood: MoodModel? = null)
        fun navigateToMap()
        fun navigateToInsights()
        fun applyNightMode(enabled: Boolean)
        fun finishView()
    }

    interface Presenter {
        fun loadMoods()
        fun openAddMood()
        fun openEditMood(mood: MoodModel)
        fun openMap()
        fun openInsights()
        fun loadNightMode()
        fun doToggleNightMode()
        fun launchEditor(intent: android.content.Intent)
    }
}
