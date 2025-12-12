package org.wit.mood.views.moodlist

import android.content.Intent
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import org.wit.mood.activities.MoodMapActivity
import org.wit.mood.activities.MoodView
import org.wit.mood.activities.InsightsActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MoodListPresenter(private val view: MoodListView) {

    private val app: MainApp = view.application as MainApp

    private val dayFmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun loadMoods(): List<MoodModel> =
        app.moods.findAll()

    fun groupByDay(moods: List<MoodModel>): List<DailyMoodSummary> {
        return moods
            .groupBy { it.timestamp.take(10) }
            .map { (date, moods) ->
                val avg = moods.map { it.type.score }.average()
                DailyMoodSummary(date, moods, avg)
            }
            .sortedByDescending { it.date }
    }

    fun openAddMood() {
        view.launchMoodEditor(null)
    }

    fun openEditMood(mood: MoodModel) {
        view.launchMoodEditor(mood)
    }

    fun openMap() {
        view.startActivity(Intent(view, MoodMapActivity::class.java))
    }

    fun openInsights() {
        view.startActivity(Intent(view, InsightsActivity::class.java))
        view.finish()
    }
}
