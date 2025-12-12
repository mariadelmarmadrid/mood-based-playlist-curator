package org.wit.mood.views.moodlist

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.wit.mood.activities.InsightsActivity
import org.wit.mood.activities.MoodMapActivity
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import org.wit.mood.views.mood.MoodView

class MoodListPresenter(private val view: MoodListView) {

    private val app: MainApp = view.application as MainApp

    private lateinit var moodEditorLauncher: ActivityResultLauncher<Intent>

    init {
        registerMoodEditorCallback()
    }

    fun loadMoods(): List<MoodModel> = app.moods.findAll()

    fun groupByDay(moods: List<MoodModel>): List<DailyMoodSummary> {
        return moods
            .groupBy { it.timestamp.take(10) }
            .map { (date, moodsInDay) ->
                val sorted = moodsInDay.sortedByDescending { it.timestamp }
                val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
                DailyMoodSummary(date, sorted, avg)
            }
            .sortedByDescending { it.date }
    }

    fun openAddMood() {
        moodEditorLauncher.launch(Intent(view, MoodView::class.java))
    }

    fun openEditMood(mood: MoodModel) {
        val intent = Intent(view, MoodView::class.java).putExtra("mood_edit", mood)
        moodEditorLauncher.launch(intent)
    }

    fun openMap() {
        view.startActivity(Intent(view, MoodMapActivity::class.java))
    }

    fun openInsights() {
        view.startActivity(Intent(view, InsightsActivity::class.java))
        view.finish()
    }

    private fun registerMoodEditorCallback() {
        moodEditorLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    view.renderList()  // ✅ refresh immediately after add/edit
                }
            }
    }
}
